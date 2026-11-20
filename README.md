# MS etudiant-backend

Backend qui gère les APIs des utilisateurs de la bibliothèque et les CRUD des étudiants.

## Configuration du backend

    - name: etudiant-backend
    - port: 8080

## Pré-requis pour le bon fonctionnement du service :

    -> JDK 21
    -> Docker
    -> Docker Compose
    -> Maven 3.9.3 (https://archive.apache.org/dist/maven/maven-3/3.9.3/binaries/) ou plus

## Démarrage du backend
Pour démarrer le projet backend, il faut : 
- avoir démarré Docker-Desktop sur votre poste de travail local.
- dans une console, se placer à la racine du projet et exécuter la commande Maven suivante :
```
mvn spring-boot:run
```

Cette commande va : 
 - initialiser le container Docker qui contient la base de données 
 - lancer le serveur du backend et le connecter à la base de données précédemment créée

Les traces logs devraient ressemblées à ceci : 
```
.   ____          _            __ _ _
/\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
\\/  ___)| |_)| | | | | || (_| |  ) ) ) )
'  |____| .__|_| |_|_| |_\__, | / / / /
=========|_|==============|___/=/_/_/_/

:: Spring Boot ::                (v3.5.5)

[etudiant-backend] [           main] c.o.etudiant.EtudiantBackendApplication  : Starting EtudiantBackendApplication using Java 21.0.3 with PID 6964
[etudiant-backend] [           main] c.o.etudiant.EtudiantBackendApplication  : No active profile set, falling back to 1 default profile: "default"
[etudiant-backend] [           main] .s.b.d.c.l.DockerComposeLifecycleManager : Using Docker Compose file ******etudiant-backend\compose.yaml*****
[etudiant-backend] [utReader-stderr] o.s.boot.docker.compose.core.DockerCli   :  Container etudiant-backend-mysql-1  Created
[etudiant-backend] [utReader-stderr] o.s.boot.docker.compose.core.DockerCli   :  Container etudiant-backend-mysql-1  Starting
[etudiant-backend] [utReader-stderr] o.s.boot.docker.compose.core.DockerCli   :  Container etudiant-backend-mysql-1  Started
[etudiant-backend] [utReader-stderr] o.s.boot.docker.compose.core.DockerCli   :  Container etudiant-backend-mysql-1  Waiting
[etudiant-backend] [utReader-stderr] o.s.boot.docker.compose.core.DockerCli   :  Container etudiant-backend-mysql-1  Healthy
[etudiant-backend] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
[etudiant-backend] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 39 ms. Found 1 JPA repository interface.
[etudiant-backend] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
[etudiant-backend] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
[etudiant-backend] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.44]
[etudiant-backend] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
[etudiant-backend] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1354 ms
[etudiant-backend] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
[etudiant-backend] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.26.Final
[etudiant-backend] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
[etudiant-backend] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
[etudiant-backend] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
[etudiant-backend] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection com.mysql.cj.jdbc.ConnectionImpl@4db16677
[etudiant-backend] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
[etudiant-backend] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
[etudiant-backend] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
[etudiant-backend] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
[etudiant-backend] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
[etudiant-backend] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
[etudiant-backend] [           main] eAuthenticationProviderManagerConfigurer : Global AuthenticationManager configured with AuthenticationProvider bean with name authenticationProvider
[etudiant-backend] [           main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with an AuthenticationProvider bean. UserDetailsService beans w
ill not be used by Spring Security for automatically configuring username/password login. Consider removing the AuthenticationProvider bean. Alternatively, consider using the UserDetailsService in a manually instantiated DaoAuth
enticationProvider. If the current configuration is intentional, to turn off this warning, increase the logging level of 'org.springframework.security.config.annotation.authentication.configuration.InitializeUserDetailsBeanManagerConfigurer' to ERROR
[etudiant-backend] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
[etudiant-backend] [           main] c.o.etudiant.EtudiantBackendApplication  : Started EtudiantBackendApplication in 10.27 seconds (process running for 10.642)
```

Sur Docker-Desktop, vous devriez voir apparaître un container MySQL qui correspond au projet.

![1-docker-desktop](pictures/1-docker-desktop.png)

Vous pouvez vous connecter à la base de données et vérifier que la table ```user``` a été créée automatiquement.
Pour cela, cliquez sur le lien `mysql-1` ce qui vous amènera sur la vue complète de la base de données. 
Dans l'onglet ```Exec```, il faut : 

1. se connecter à la base de données. Tapez la commande ci-dessous

    ```
    mysql -u etudiant_db -p
    ```
   L'invite de commande demandera le mot de passe. Il est identique au nom d'utilisateur, c'est-à-dire ```etudiant_db```.


2. Se connecter au schéma de base de données `etudiant_db`. Dans l'invite de commande, tapez la commande ci-dessous :

    ```
    use etudiant_db;
    ```
  
3. Vérifier que la table `user` existe (elle est néanmoins vide pour le moment).

    ```
    select * from user;
    ```
    Le résultat devrait être : `Empty set (0.00 sec)`

La capture d'écran ci-dessous résume les étapes précédentes : 

![2-docker-desktop-bdd](pictures/2-docker-desktop-bdd.png)


## Configuration locale (`.env`)

Les paramètres sensibles ne sont pas versionnés. Le dépôt fournit un modèle, `.env.example`, à copier en `.env` à la racine du projet :

```
cp .env.example .env
```

Variables à renseigner :

| Variable | Rôle |
|----------|------|
| `DB_USER`, `DB_PASSWORD` | Identifiants de la base MySQL |
| `DB_HOST`, `DB_PORT`, `DB_NAME` | Hôte, port et nom de la base |
| `JWT_SECRET` | Clé secrète de signature des JWT, encodée en Base64 |

Remplacez les valeurs d'exemple (`CHANGE_ME`, `REPLACE_WITH_BASE64_ENCODED_SECRET`) par vos propres valeurs. Le fichier `.env` ne doit jamais être commité, et aucun secret ne doit apparaître dans le code ni dans les logs.

**Port MySQL local :** dans notre configuration Docker, MySQL est exposé sur le port **3307** de la machine hôte (et non 3306), afin d'éviter un conflit avec une éventuelle instance MySQL locale. `DB_PORT` doit donc valoir `3307`.

## Authentification JWT

L'API est sécurisée par JSON Web Token (JWT) :

1. Créer un compte : `POST /api/register` (retourne `201 Created`).
2. Se connecter : `POST /api/login` avec un corps JSON contenant `login` et `password`. En cas de succès (`200 OK`), le corps de la réponse contient le JWT.
3. Appeler les routes protégées en ajoutant l'en-tête HTTP suivant :

```
Authorization: Bearer <token>
```

Sans jeton, ou avec un jeton invalide ou expiré, l'accès aux routes protégées est refusé (`401 Unauthorized`).
La clé de signature est lue depuis la variable d'environnement `JWT_SECRET` (jamais codée en dur).

## API des étudiants

Toutes les routes ci-dessous sont protégées par `Authorization: Bearer <token>`. Les échanges utilisent des DTO ; un étudiant est décrit par `firstName`, `lastName` et `email` (tous obligatoires, `email` doit être valide, 255 caractères maximum).

| Méthode | Route | Description | Codes HTTP principaux |
|---------|-------|-------------|-----------------------|
| `GET` | `/api/students` | Lister les étudiants | `200`, `401` |
| `GET` | `/api/students/{id}` | Consulter le détail d'un étudiant | `200`, `401`, `404` |
| `POST` | `/api/students` | Créer un étudiant | `201` (en-tête `Location`), `400`, `401`, `409` |
| `PUT` | `/api/students/{id}` | Modifier un étudiant | `200`, `400`, `401`, `404`, `409` |
| `DELETE` | `/api/students/{id}` | Supprimer un étudiant | `204`, `401`, `404` |

Signification des codes : `400` données invalides, `401` non authentifié, `404` étudiant introuvable, `409` conflit (par exemple e-mail déjà utilisé).

## Exécution des tests
Pour exécuter les tests Junit, il faut :
- avoir démarré Docker-Desktop sur votre poste de travail local. Cette étape est nécessaire car les tests d'intégration auront besoin de Docker pour créer des bases de données temporaires de test.
- dans une console, se placer à la racine du projet et exécuter la commande Maven suivante :

```
mvn clean test
```

### Résultats des tests

- **98 tests JUnit réussis**
- Couverture de code JaCoCo :
  - **96,3 %** des lignes (LINE)
  - **82,1 %** des branches (BRANCH)
- Rapport HTML JaCoCo : `target/site/jacoco/index.html` (généré après `mvn clean test`).

Les routes ont également été vérifiées manuellement avec Postman : `200`, `201`, `204`, `401`, `404` et `409`.

## Fonctionnalités portées

    - API de création d'un utilisateur (agent de la bibliothèque)
    - API d'authentification d'un utilisateur, avec génération d'un JWT (réalisé)
    - APIs CRUD des étudiants de la bibliothèque, protégées par JWT Bearer (réalisé)
    - Validation des données via DTO et gestion des erreurs métier (404, 409)
    - Tests JUnit et couverture JaCoCo (réalisé)


## Écrans ou blocs concernés
    - Ecran xxx
    - Ecran xxx
    - Ecran xxx



