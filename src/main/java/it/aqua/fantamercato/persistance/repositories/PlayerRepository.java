package it.aqua.fantamercato.persistance.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import it.aqua.fantamercato.persistance.models.Player;

public interface PlayerRepository extends MongoRepository<Player, String> {

	public List<Player> findByRole(String role);
	public List<Player> deleteByRole(String role);
		
}
