package com.nikoloz.secureapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
// Exclude owner from toString to avoid lazy-loading the associated AppUser during logging
@ToString(exclude = "owner")
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{validation.task.title.notblank}")
    @Size(min = 1, max = 200, message = "{validation.task.title.size}")
    @Column(nullable = false)
    private String title;

    @Size(max = 1000, message = "{validation.task.description.size}")
    private String description;

    @Column(nullable = false)
    private boolean completed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    public Task(String title, String description, AppUser owner) {
        this.title       = title;
        this.description = description;
        this.owner       = owner;
        this.completed   = false;
    }
}
