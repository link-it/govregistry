package it.govhub.rest.backoffice.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data	
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "users")
public class UserEntity {

	@Id
	@SequenceGenerator(name="seq_users",sequenceName="seq_users", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_users")
	private Long id;

	@Column(name = "principal", unique=true, nullable = false)
	private String principal;

	@Column(name = "full_name", nullable = false)
	private String full_name;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "enabled", nullable=false)
	private Boolean enabled;

}