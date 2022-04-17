package ru.makhmudov.search_engine.entity;

import javax.persistence.*;

@Entity
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "lemma", nullable = false)
    private String lemma;

//    @Column(name = "frequenc")
}
