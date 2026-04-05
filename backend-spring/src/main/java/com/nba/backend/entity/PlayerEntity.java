package com.nba.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "dim_player")
public class PlayerEntity {

  @Id
  @Column(name = "player_id")
  private Long id;

  @Column(name = "full_name", nullable = false, length = 120)
  private String name;

  @Column(name = "first_name", length = 60)
  private String firstName;

  @Column(name = "last_name", length = 60)
  private String lastName;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column(name = "height_inches")
  private Integer heightInches;

  @Column(name = "weight_lbs")
  private Integer weightLbs;

  @Column(length = 20)
  private String position;

  @Column(length = 50)
  private String country;

  @Column(name = "draft_year")
  private Integer draftYear;

  @Column(name = "draft_round")
  private Integer draftRound;

  @Column(name = "draft_number")
  private Integer draftNumber;

  @Column(name = "is_active")
  private Boolean isActive = true;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public Integer getHeightInches() {
    return heightInches;
  }

  public void setHeightInches(Integer heightInches) {
    this.heightInches = heightInches;
  }

  public Integer getWeightLbs() {
    return weightLbs;
  }

  public void setWeightLbs(Integer weightLbs) {
    this.weightLbs = weightLbs;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public Integer getDraftYear() {
    return draftYear;
  }

  public void setDraftYear(Integer draftYear) {
    this.draftYear = draftYear;
  }

  public Integer getDraftRound() {
    return draftRound;
  }

  public void setDraftRound(Integer draftRound) {
    this.draftRound = draftRound;
  }

  public Integer getDraftNumber() {
    return draftNumber;
  }

  public void setDraftNumber(Integer draftNumber) {
    this.draftNumber = draftNumber;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }
}
