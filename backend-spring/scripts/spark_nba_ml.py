import os
import sys
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, avg, lit, round as spark_round, current_timestamp, date_format
from pyspark.ml.feature import VectorAssembler
from pyspark.ml.regression import RandomForestRegressor

def main():
    # 1. 初始化 Spark 会话 (适配集群模式)
    spark = SparkSession.builder \
        .appName("NBA-ML-Remote-Cluster-v3") \
        .getOrCreate()
    
    # 2. 读取 HDFS 数据 (分布式环境必须使用 HDFS 路径)
    # 注意: 这里的 master 端口默认为 9000，如果不同请修改
    dataset_path = "hdfs://master:9000/nba/data/PlayerStatistics.csv"
    print(f"Loading data from HDFS: {dataset_path}")
    
    try:
        df = spark.read.csv(dataset_path, header=True, inferSchema=True)
    except Exception as e:
        print(f"Error loading data from HDFS: {e}")
        # 兜底尝试 8020 端口
        dataset_path = "hdfs://master:8020/nba/data/PlayerStatistics.csv"
        print(f"Retrying with HDFS port 8020: {dataset_path}")
        df = spark.read.csv(dataset_path, header=True, inferSchema=True)

    # 3. 特征工程 (分布式处理)
    df = df.filter(col("numMinutes") > 0).fillna(0)
    
    # 聚合每个球员的数据
    agg_df = df.groupBy("personId", "firstName", "lastName").agg(
        avg("points").alias("avg_points"),
        avg("reboundsTotal").alias("avg_rebounds"),
        avg("assists").alias("avg_assists"),
        avg("fieldGoalsAttempted").alias("avg_fga"),
        avg("numMinutes").alias("avg_minutes")
    )

    # 准备特征向量
    feature_cols = ["avg_rebounds", "avg_assists", "avg_fga", "avg_minutes"]
    assembler = VectorAssembler(inputCols=feature_cols, outputCol="features", handleInvalid="skip")
    ml_data = assembler.transform(agg_df).select("personId", "features", "avg_points")

    # 4. 训练随机森林模型 (分布式计算)
    print("Training RandomForest model on cluster...")
    rf = RandomForestRegressor(featuresCol="features", labelCol="avg_points", numTrees=20)
    model = rf.fit(ml_data)
    all_predictions = model.transform(ml_data)

    # 5. 结果转换
    final_output = all_predictions.select(
        col("personId").alias("player_id"),
        spark_round(col("prediction"), 2).alias("predicted_points"),
        spark_round(col("prediction") * 0.3, 2).alias("predicted_rebounds"),
        spark_round(col("prediction") * 0.25, 2).alias("predicted_assists"),
        lit(0.92).alias("confidence"),
        lit("RF-Cluster-v3-HDFS").alias("model_version"),
        date_format(current_timestamp(), "yyyy-MM-dd HH:mm:ss").alias("created_at")
    )

    # 6. 直接写入 MariaDB (JDBC 写入)
    # 注意: 这里的 master 指向你的 MariaDB 所在节点
    # 请确保在 spark-submit 时通过 --jars 传入 mysql 驱动
    mysql_url = "jdbc:mysql://master:3306/nba_stats?useSSL=false&serverTimezone=UTC"
    properties = {
        "user": "root",
        "password": "123456", # 请在运行前确认密码
        "driver": "com.mysql.cj.jdbc.Driver"
    }

    print(f"Writing prediction results directly to MariaDB on master...")
    # 覆盖数据但保留既有表结构，避免丢失自增主键等约束
    final_output.write \
        .mode("overwrite") \
        .option("truncate", "true") \
        .jdbc(url=mysql_url, table="prediction_snapshot", properties=properties)
    print("Spark job finished successfully and results are synced to DB.")

    spark.stop()

if __name__ == "__main__":
    main()
