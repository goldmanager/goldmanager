package com.my.goldmanager.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "item")
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Getter
	@Setter
	private UUID id;

	@Setter
	@Getter
	@Column
	private String name;

	@Setter
	@Getter
	@Column
	private float amount_oz;

	@ManyToOne
	@JoinColumn(name = "type")
	@Setter
	@Getter
	private ItemType itemType;
}
