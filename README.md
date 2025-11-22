# Mentor de Redação Zen - Backend API

API REST desenvolvida em Java com Spring Boot para o sistema Mentor de Redação Zen, uma solução inovadora que utiliza Inteligência Artificial para apoiar estudantes do ensino médio no desenvolvimento de suas habilidades de escrita para o ENEM, com foco especial em saúde mental e bem-estar emocional.

## Índice
- [Documentação da API](#documentação-da-api)
- [Sobre o Projeto](#sobre-o-projeto)
- [Arquitetura](#arquitetura)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Pré-requisitos](#pré-requisitos)
- [Instalação e Configuração](#instalação-e-configuração)
- [Configuração do Banco de Dados](#configuração-do-banco-de-dados)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Executando a Aplicação](#executando-a-aplicação)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Segurança](#segurança)
- [Troubleshooting](#troubleshooting)

## Documentação da API

### Acessando o Swagger UI

A documentação interativa da API está disponível através do Swagger UI:

1. **Inicie a aplicação** (se ainda não iniciou)

2. **Acesse no navegador:**
   ```
   http://localhost:8080/swagger-ui.html
   ```

3. **Autenticação no Swagger:**
    - Clique no botão "Authorize" no topo da página
    - Faça login através do endpoint `/auth/login` para obter um token JWT
    - Cole o token no formato: `Bearer <seu_token_jwt>`
    - Clique em "Authorize" e depois em "Close"

4. **Testando Endpoints:**
    - Expanda qualquer endpoint clicando nele
    - Clique em "Try it out"
    - Preencha os parâmetros necessários
    - Clique em "Execute"
    - Veja a resposta abaixo
    -
## Sobre o Projeto

O Mentor de Redação Zen é uma plataforma educacional desenvolvida para o Hackathon Gemini for Education 2024. O sistema oferece feedback automatizado e empático sobre redações, utilizando a API do Google Gemini AI para análise baseada nas 5 competências do ENEM, sempre priorizando o bem-estar mental dos estudantes.

### Funcionalidades Principais

- **Autenticação e Autorização**: Sistema completo de autenticação JWT com Spring Security
- **Gestão de Redações**: CRUD completo para criação, edição e análise de redações
- **Análise com IA**: Integração com Google Gemini AI para feedback detalhado
- **Sistema de Feedbacks**: Armazenamento e recuperação de feedbacks estruturados
- **Perfil de Usuário**: Personalização completa do perfil com upload de foto
- **Recuperação de Senha**: Sistema de tokens para recuperação segura de senha
- **Migrações de Banco**: Gerenciamento de schema com Liquibase

## Arquitetura

O projeto segue os princípios de **Clean Architecture** e **SOLID**, organizando o código em camadas bem definidas:

### Camadas da Aplicação

```
com.mentorzen/
├── domain/              # Camada de Domínio (Entidades e Regras de Negócio)
│   ├── entity/         # Entidades JPA (User, Essay, Feedback, etc.)
│   └── repository/     # Interfaces de Repositório (Spring Data JPA)
│
├── application/         # Camada de Aplicação (Casos de Uso)
│   ├── dto/            # Data Transfer Objects (Request/Response)
│   └── service/        # Interfaces e Implementações de Serviços
│
├── infrastructure/      # Camada de Infraestrutura (Implementações Técnicas)
│   ├── config/         # Configurações (Security, Swagger, CORS, etc.)
│   └── exception/      # Exceções Customizadas
│
└── presentation/        # Camada de Apresentação (Controllers REST)
    └── controller/     # Endpoints da API
```

### Decisões de Arquitetura

**Por que Clean Architecture?**

- **Separação de Responsabilidades**: Cada camada tem uma responsabilidade clara e bem definida
- **Testabilidade**: Facilita a criação de testes unitários e de integração
- **Manutenibilidade**: Mudanças em uma camada não afetam diretamente outras camadas
- **Independência de Frameworks**: A lógica de negócio não depende de frameworks específicos
- **Flexibilidade**: Facilita a troca de tecnologias (ex: trocar JPA por outro ORM)

**Por que Spring Boot?**

- **Produtividade**: Reduz significativamente a configuração boilerplate
- **Ecossistema Maduro**: Amplo suporte da comunidade e documentação
- **Integração Nativa**: Integração fácil com PostgreSQL, Liquibase, Swagger, etc.
- **Spring Security**: Framework robusto e amplamente utilizado para segurança
- **Spring Data JPA**: Abstração poderosa para acesso a dados

**Por que PostgreSQL?**

- **Open Source**: Licença livre e sem custos
- **Robustez**: Banco de dados relacional maduro e confiável
- **Performance**: Excelente desempenho para aplicações web
- **Recursos Avançados**: Suporte a JSON, arrays, full-text search, etc.
- **Comunidade Ativa**: Grande comunidade e suporte

**Por que Liquibase?**

- **Versionamento de Schema**: Controle de versão para mudanças no banco de dados
- **Rollback**: Capacidade de reverter migrações quando necessário
- **Multi-ambiente**: Mesmas migrações funcionam em dev, staging e produção
- **Rastreabilidade**: Histórico completo de todas as mudanças no schema
- **Colaboração**: Facilita trabalho em equipe com mudanças coordenadas

**Por que JWT (JSON Web Tokens)?**

- **Stateless**: Não requer armazenamento de sessão no servidor
- **Escalabilidade**: Facilita escalonamento horizontal
- **Segurança**: Tokens assinados e opcionalmente criptografados
- **Padrão da Indústria**: Amplamente adotado e suportado
- **OAuth2 Resource Server**: Integração nativa com Spring Security

## Tecnologias Utilizadas

### Core
- **Java 17**: Linguagem de programação
- **Spring Boot 3.2.0**: Framework principal
- **Maven**: Gerenciador de dependências

### Banco de Dados
- **PostgreSQL**: Banco de dados relacional
- **Spring Data JPA**: Abstração para acesso a dados
- **Hibernate**: ORM (Object-Relational Mapping)
- **Liquibase 4.30.0**: Gerenciamento de migrações de banco

### Segurança
- **Spring Security**: Framework de segurança
- **OAuth2 Resource Server**: Autenticação baseada em JWT
- **BCrypt**: Hash de senhas
- **JWT (Nimbus)**: Tokens de autenticação

### Documentação
- **SpringDoc OpenAPI 3.2.5.0**: Documentação automática da API
- **Swagger UI**: Interface interativa para testar a API

### Integração com IA
- **Google Cloud AI Platform 3.31.0**: SDK para Google Gemini AI
- **Spring WebFlux**: Cliente HTTP reativo para chamadas à API

### Utilitários
- **Lombok**: Redução de boilerplate
- **Spring Boot DevTools**: Ferramentas de desenvolvimento
- **Bean Validation**: Validação de dados

## Pré-requisitos

Antes de começar, certifique-se de ter instalado:

1. **Java 17 ou superior**
   ```bash
   java -version
   ```
   Se não tiver instalado, baixe em: https://adoptium.net/

2. **Maven 3.8 ou superior**
   ```bash
   mvn -version
   ```
   Se não tiver instalado, baixe em: https://maven.apache.org/download.cgi

3. **PostgreSQL 12 ou superior**
   ```bash
   psql --version
   ```
   Se não tiver instalado, baixe em: https://www.postgresql.org/download/

4. **Git** (opcional, para clonar o repositório)
   ```bash
   git --version
   ```

## Instalação e Configuração

### 1. Clonar o Repositório

```bash
git clone <url-do-repositorio>
cd MentorZen/backend
```

### 2. Configurar o Banco de Dados PostgreSQL

#### 2.1. Iniciar o PostgreSQL

Certifique-se de que o PostgreSQL está rodando:

**Windows:**
```bash
# Verificar se o serviço está rodando
sc query postgresql-x64-XX

# Se não estiver, iniciar o serviço
net start postgresql-x64-XX
```

**Linux/Mac:**
```bash
# Verificar status
sudo systemctl status postgresql

# Iniciar se necessário
sudo systemctl start postgresql
```

#### 2.2. Criar o Banco de Dados

Conecte-se ao PostgreSQL como superusuário:

```bash
psql -U postgres
```

Execute os seguintes comandos SQL:

```sql
-- Criar banco de dados de desenvolvimento
CREATE DATABASE mentor_zen_dev;

-- Criar usuário (opcional, mas recomendado)
CREATE USER mentor_zen_user WITH PASSWORD 'mentor_zen_pass';

-- Conceder privilégios
GRANT ALL PRIVILEGES ON DATABASE mentor_zen_dev TO mentor_zen_user;

-- Conectar ao banco criado
\c mentor_zen_dev

-- Conceder privilégios no schema public
GRANT ALL ON SCHEMA public TO mentor_zen_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mentor_zen_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO mentor_zen_user;

-- Sair do psql
\q
```

**Nota**: Se preferir usar o usuário `postgres` padrão, você pode pular a criação do usuário e usar as credenciais padrão.

### 3. Configurar Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto backend (opcional, você também pode exportar as variáveis diretamente):

```bash
# .env (opcional - para desenvolvimento local)
DATABASE_URL=jdbc:postgresql://localhost:5432/mentor_zen_dev
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
JWT_SECRET=sua_chave_jwt_segura_para_desenvolvimento
GOOGLE_AI_API_KEY=sua_api_key_do_google
GOOGLE_AI_PROJECT_ID=seu_project_id_do_google
```

**Importante**: O arquivo `.env` não deve ser commitado no Git. Ele já está no `.gitignore`.

Alternativamente, você pode exportar as variáveis diretamente no terminal:

**Windows (PowerShell):**
```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/mentor_zen_dev"
$env:DATABASE_USERNAME="postgres"
$env:DATABASE_PASSWORD="postgres"
$env:JWT_SECRET="sua_chave_jwt_segura_para_desenvolvimento"
```

**Linux/Mac:**
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/mentor_zen_dev
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
export JWT_SECRET=sua_chave_jwt_segura_para_desenvolvimento
```

### 4. Gerar JWT Secret Seguro

Para desenvolvimento, você pode usar um secret simples. Para produção, **SEMPRE** gere um secret seguro:

```bash
# Usando OpenSSL
openssl rand -base64 32
```

Copie o resultado e use como valor de `JWT_SECRET`.

## Configuração do Banco de Dados

### Perfis de Ambiente

O projeto utiliza perfis do Spring Boot para diferentes ambientes:

- **dev**: Ambiente de desenvolvimento (padrão)
- **prod**: Ambiente de produção

### Arquivos de Configuração

- `application.properties`: Configurações comuns a todos os perfis
- `application-dev.properties`: Configurações específicas de desenvolvimento
- `application-prod.properties`: Configurações específicas de produção

### Configuração de Desenvolvimento

O arquivo `application-dev.properties` já está configurado com valores padrão:

```properties
# Banco de dados
spring.datasource.url=jdbc:postgresql://localhost:5432/zen_dev
spring.datasource.username=postgres
spring.datasource.password=postgres

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Liquibase
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:/liquibase/master.xml
```

**Nota**: Se você definir as variáveis de ambiente `DATABASE_URL`, `DATABASE_USERNAME` e `DATABASE_PASSWORD`, elas terão prioridade sobre os valores do arquivo de propriedades.

### Migrações com Liquibase

O Liquibase executa automaticamente as migrações na inicialização da aplicação. As migrações estão localizadas em:

```
src/main/resources/liquibase/
├── master.xml                    # Arquivo principal que inclui todas as migrações
└── changelogs/
    ├── 001-create-users-table.xml
    ├── 002-create-essays-table.xml
    ├── 003-create-feedbacks-table.xml
    ├── 004-create-password-reset-tokens-table.xml
    └── 005-add-user-profile-fields.xml
```

**Comandos úteis do Liquibase:**

```bash
# Ver status das migrações
mvn liquibase:status

# Aplicar migrações manualmente
mvn liquibase:update

# Rollback da última migração
mvn liquibase:rollback -Dliquibase.rollbackCount=1

# Gerar changelog a partir do banco existente
mvn liquibase:generateChangeLog
```

## Variáveis de Ambiente

### Variáveis Obrigatórias (Produção)

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `JWT_SECRET` | Chave secreta para assinar tokens JWT | `openssl rand -base64 32` |
| `DATABASE_URL` | URL de conexão com PostgreSQL | `jdbc:postgresql://localhost:5432/mentor_zen_prod` |
| `DATABASE_USERNAME` | Usuário do banco de dados | `mentor_zen_user` |
| `DATABASE_PASSWORD` | Senha do banco de dados | `senha_segura_123` |

### Variáveis Opcionais

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `SPRING_PROFILES_ACTIVE` | Perfil ativo (dev/prod) | `dev` |
| `JWT_ISSUER` | Emissor do token JWT | `mentor-redacao-zen` |
| `JWT_EXPIRE_DURATION` | Duração de expiração do token | `7d` |
| `GOOGLE_AI_API_KEY` | Chave da API do Google Gemini | `apikey` |
| `GOOGLE_AI_PROJECT_ID` | ID do projeto Google Cloud | `apikey` |
| `GEMINI_MAX_REQUESTS` | Máximo de requisições por minuto | `60` |
| `GEMINI_FALLBACK_TO_MOCK` | Usar dados mock em caso de falha | `true` |

## Executando a Aplicação

### Modo Desenvolvimento

1. **Certifique-se de que o PostgreSQL está rodando**

2. **Configure as variáveis de ambiente** (se necessário)

3. **Execute a aplicação:**

```bash
# Usando Maven
mvn spring-boot:run

# Ou compilando primeiro e depois executando
mvn clean package
java -jar target/mentor-redacao-zen-0.0.1-SNAPSHOT.jar
```

4. **A aplicação estará disponível em:**
    - API: http://localhost:8080
    - Swagger UI: http://localhost:8080/swagger-ui.html
    - API Docs (JSON): http://localhost:8080/api-docs

### Modo Produção

1. **Configure todas as variáveis de ambiente obrigatórias**

2. **Ative o perfil de produção:**

```bash
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
```

3. **Ou compile e execute o JAR:**

```bash
mvn clean package -DskipTests
java -jar -Dspring.profiles.active=prod target/mentor-redacao-zen-0.0.1-SNAPSHOT.jar
```

### Verificar se está Funcionando

Após iniciar, você pode testar se a API está respondendo:

```bash
# Health check (se configurado)
curl http://localhost:8080/actuator/health

# Ou testar o endpoint de documentação
curl http://localhost:8080/api-docs
```

### Endpoints Principais

#### Autenticação
- `POST /auth/login` - Login de usuário
- `POST /auth/register` - Registro de novo usuário
- `POST /auth/forgot-password` - Solicitar recuperação de senha
- `POST /auth/reset-password` - Redefinir senha com token

#### Redações
- `GET /essays` - Listar redações do usuário
- `POST /essays` - Criar nova redação
- `GET /essays/{id}` - Obter redação por ID
- `PUT /essays/{id}` - Atualizar redação
- `DELETE /essays/{id}` - Deletar redação
- `POST /essays/{id}/analyze` - Analisar redação com IA

#### Feedbacks
- `GET /feedbacks` - Listar feedbacks
- `GET /feedbacks/{id}` - Obter feedback por ID

#### Perfil
- `GET /profile` - Obter perfil do usuário logado
- `PUT /profile` - Atualizar perfil
- `PUT /profile/password` - Alterar senha
- `POST /profile/picture` - Upload de foto de perfil
- `DELETE /profile/picture` - Remover foto de perfil
- `DELETE /profile` - Deletar conta

### Documentação JSON (OpenAPI)

A documentação em formato JSON está disponível em:
```
http://localhost:8080/api-docs
```

Você pode importar este JSON em ferramentas como Postman ou Insomnia.

## Estrutura do Projeto

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/mentorzen/
│   │   │   ├── domain/                    # Camada de Domínio
│   │   │   │   ├── entity/               # Entidades JPA
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Essay.java
│   │   │   │   │   ├── Feedback.java
│   │   │   │   │   └── PasswordResetToken.java
│   │   │   │   └── repository/           # Interfaces de Repositório
│   │   │   │       ├── UserRepository.java
│   │   │   │       ├── EssayRepository.java
│   │   │   │       ├── FeedbackRepository.java
│   │   │   │       └── PasswordResetTokenRepository.java
│   │   │   │
│   │   │   ├── application/               # Camada de Aplicação
│   │   │   │   ├── dto/                   # Data Transfer Objects
│   │   │   │   │   ├── request/          # DTOs de requisição
│   │   │   │   │   └── response/         # DTOs de resposta
│   │   │   │   └── service/               # Serviços de negócio
│   │   │   │       ├── AuthService.java
│   │   │   │       ├── EssayService.java
│   │   │   │       ├── EssayAnalysisService.java
│   │   │   │       ├── FeedbackService.java
│   │   │   │       ├── UserProfileService.java
│   │   │   │       ├── security/
│   │   │   │       │   └── JwtService.java
│   │   │   │       └── impl/              # Implementações
│   │   │   │
│   │   │   ├── infrastructure/            # Camada de Infraestrutura
│   │   │   │   ├── config/                # Configurações
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── SwaggerConfig.java
│   │   │   │   │   ├── CorsConfig.java
│   │   │   │   │   ├── WebClientConfig.java
│   │   │   │   │   ├── WebConfig.java
│   │   │   │   │   └── properties/
│   │   │   │   │       └── JwtProperties.java
│   │   │   │   └── exception/             # Exceções customizadas
│   │   │   │       ├── BusinessException.java
│   │   │   │       └── ResourceNotFoundException.java
│   │   │   │
│   │   │   ├── presentation/              # Camada de Apresentação
│   │   │   │   └── controller/            # Controllers REST
│   │   │   │       ├── AuthController.java
│   │   │   │       ├── EssayController.java
│   │   │   │       ├── FeedbackController.java
│   │   │   │       └── UserProfileController.java
│   │   │   │
│   │   │   └── MentorRedacaoZenApplication.java  # Classe principal
│   │   │
│   │   └── resources/
│   │       ├── application.properties           # Configurações comuns
│   │       ├── application-dev.properties       # Configurações de dev
│   │       ├── application-prod.properties      # Configurações de prod
│   │       └── liquibase/                       # Migrações de banco
│   │           ├── master.xml
│   │           └── changelogs/
│   │               ├── 001-create-users-table.xml
│   │               ├── 002-create-essays-table.xml
│   │               ├── 003-create-feedbacks-table.xml
│   │               ├── 004-create-password-reset-tokens-table.xml
│   │               └── 005-add-user-profile-fields.xml
│   │
│   └── test/                                 # Testes (estrutura similar)
│
├── pom.xml                                   # Configuração Maven
├── README.md                                 # Este arquivo
├── LICENSE                                   # Licença do projeto
└── SETUP_DATABASE.md                         # Documentação adicional do banco
```

## Segurança

### Autenticação JWT

A aplicação utiliza JWT (JSON Web Tokens) para autenticação stateless. O fluxo é:

1. Usuário faz login através de `/auth/login`
2. Servidor valida credenciais e retorna um token JWT
3. Cliente inclui o token no header `Authorization: Bearer <token>`
4. Servidor valida o token em cada requisição protegida

### Endpoints Públicos

Os seguintes endpoints são públicos (não requerem autenticação):

- `/auth/login`
- `/auth/register`
- `/swagger-ui.html`
- `/swagger-ui/**`
- `/api-docs/**`
- `/v3/api-docs/**`
- `/actuator/health`

Todos os outros endpoints requerem autenticação.

### Configuração de Segurança

A configuração de segurança está em `SecurityConfig.java`:

- **CORS**: Configurado para permitir requisições do frontend
- **CSRF**: Desabilitado (não necessário para APIs stateless)
- **OAuth2 Resource Server**: Configurado para validar JWT tokens
- **Password Encoding**: BCrypt com força 10

### Boas Práticas de Segurança

1. **JWT Secret**:
    - NUNCA commitar no código
    - Usar variáveis de ambiente
    - Gerar com pelo menos 256 bits (32 caracteres)
    - Rotacionar periodicamente em produção

2. **Senhas**:
    - Sempre hashadas com BCrypt
    - Nunca armazenadas em texto plano
    - Validação de força mínima

3. **CORS**:
    - Em produção, restringir origins permitidos
    - Não usar `*` em produção

4. **HTTPS**:
    - SEMPRE usar HTTPS em produção
    - Nunca transmitir tokens JWT via HTTP

## Troubleshooting

### Erro: "Connection refused" ao conectar ao PostgreSQL

**Causa**: PostgreSQL não está rodando ou porta incorreta.

**Solução**:
1. Verifique se o PostgreSQL está rodando:
   ```bash
   # Windows
   sc query postgresql-x64-XX
   
   # Linux/Mac
   sudo systemctl status postgresql
   ```

2. Verifique a porta padrão (5432):
   ```bash
   # Windows
   netstat -an | findstr 5432
   
   # Linux/Mac
   netstat -an | grep 5432
   ```

3. Verifique a URL de conexão no `application-dev.properties`

### Erro: "Database does not exist"

**Causa**: Banco de dados não foi criado.

**Solução**:
1. Conecte-se ao PostgreSQL:
   ```bash
   psql -U postgres
   ```

2. Crie o banco:
   ```sql
   CREATE DATABASE mentor_zen_dev;
   ```

### Erro: "Liquibase changelog not found"

**Causa**: Caminho do changelog incorreto ou arquivo não existe.

**Solução**:
1. Verifique se o arquivo `master.xml` existe em `src/main/resources/liquibase/`
2. Verifique se o caminho em `application-dev.properties` está correto:
   ```properties
   spring.liquibase.change-log=classpath:/liquibase/master.xml
   ```

### Erro: "JWT secret is null"

**Causa**: Variável de ambiente `JWT_SECRET` não está definida.

**Solução**:
1. Defina a variável de ambiente:
   ```bash
   export JWT_SECRET=sua_chave_secreta_aqui
   ```

2. Ou adicione no `application-dev.properties` (apenas para desenvolvimento):
   ```properties
   jwt.secret=sua_chave_secreta_aqui
   ```

### Erro: "Port 8080 already in use"

**Causa**: Outra aplicação está usando a porta 8080.

**Solução**:
1. Encontre o processo usando a porta:
   ```bash
   # Windows
   netstat -ano | findstr :8080
   
   # Linux/Mac
   lsof -i :8080
   ```

2. Encerre o processo ou mude a porta da aplicação:
   ```properties
   server.port=8081
   ```

### Erro: "Table already exists" no Liquibase

**Causa**: Tabelas já existem no banco, mas o Liquibase não tem registro.

**Solução**:
1. Verifique o status:
   ```bash
   mvn liquibase:status
   ```

2. Se necessário, marque as migrações como executadas:
   ```bash
   mvn liquibase:changelogSync
   ```

3. Ou limpe o banco e execute novamente (apenas em desenvolvimento):
   ```sql
   DROP SCHEMA public CASCADE;
   CREATE SCHEMA public;
   ```

### Erro ao fazer upload de foto de perfil

**Causa**: Diretório de uploads não existe ou sem permissões.

**Solução**:
1. Crie o diretório manualmente:
   ```bash
   mkdir -p uploads/profile-pictures
   ```

2. Verifique permissões de escrita no diretório

### Swagger UI não carrega

**Causa**: Dependência do SpringDoc não está no classpath ou configuração incorreta.

**Solução**:
1. Verifique se a dependência está no `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.springdoc</groupId>
       <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
       <version>2.5.0</version>
   </dependency>
   ```

2. Verifique as configurações em `application.properties`:
   ```properties
   springdoc.api-docs.path=/api-docs
   springdoc.swagger-ui.path=/swagger-ui.html
   ```

3. Limpe e recompile:
   ```bash
   mvn clean package
   ```

## Suporte

Para questões, problemas ou sugestões, abra uma issue no repositório do projeto.

## Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## Equipe

Desenvolvido pela equipe **FloWrite** para o Hackathon Gemini for Education 2024:

- Eduardo Mello Garcia
- Gabriel Oliveira de Matos
- João Gabriel Abreu Baumhardt da Silva
- João Vitor Prestes Grando

