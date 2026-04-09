package com.beta.FindHome.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Column(name = "district", nullable = false, length = 255)
    private String district;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "ward", nullable = false, length = 100)
    private String ward;

    @Column(name = "tole", nullable = false, length = 100)
    private String tole;
}