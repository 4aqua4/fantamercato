package it.aqua.fantamercato.persistance.config;

import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = {"it.aqua.fantamercato.persistance.repositories"})
public class MongoDbConfig {

}
