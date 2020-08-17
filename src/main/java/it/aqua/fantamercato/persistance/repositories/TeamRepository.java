package it.aqua.fantamercato.persistance.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import it.aqua.fantamercato.persistance.models.Team;

public interface TeamRepository extends MongoRepository<Team, String> {

	public List<Team> findByStatus(int status);
	
	public Team findByUsername(String usernme);
	
}
