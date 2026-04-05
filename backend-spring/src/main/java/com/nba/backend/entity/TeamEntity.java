package com.nba.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dim_team")
public class TeamEntity {

  @Id
  @Column(name = "team_id")
  private Long id;

  @Column(nullable = false, length = 10, unique = true)
  private String abbreviation;

  @Column(name = "team_name", nullable = false, length = 80)
  private String name;

  @Column(length = 80)
  private String city;

  @Column(length = 20)
  private String conference;

  @Column(length = 30)
  private String division;

  @Column(name = "is_active")
  private Boolean isActive = true;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAbbreviation() {
    return abbreviation;
  }

  public void setAbbreviation(String abbreviation) {
    this.abbreviation = abbreviation;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getConference() {
    return conference;
  }

  public void setConference(String conference) {
    this.conference = conference;
  }

  public String getDivision() {
    return division;
  }

  public void setDivision(String division) {
    this.division = division;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }
}
