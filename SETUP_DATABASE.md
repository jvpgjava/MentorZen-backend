# Setup do Banco de Dados - Mentor Zen

## Pré-requisitos

- PostgreSQL 12+ instalado
- Java 17+
- Maven 3.8+

## Configuração do PostgreSQL

### 1. Criar o banco de dados de desenvolvimento

```sql
-- Conectar como postgres
psql -U postgres

-- Criar banco de desenvolvimento
CREATE DATABASE mentor_zen_dev;

-- Criar usuário (opcional)
CREATE USER mentor_zen_user WITH PASSWORD 'mentor_zen_pass';
GRANT ALL PRIVILEGES ON DATABASE mentor_zen_dev TO mentor_zen_user;
```

### 2. Configurar variáveis de ambiente (Desenvolvimento)

```bash
# Banco de dados (opcional - se não definidas, usará os valores padrão)
export DATABASE_URL=jdbc:postgresql://localhost:5432/mentor_zen_dev
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres

# JWT Secret (OBRIGATÓRIO para produção, opcional para dev)
# Para desenvolvimento local, um secret padrão será usado automaticamente
# Para produção, SEMPRE definir um secret forte e único
export JWT_SECRET=sua_chave_jwt_segura_para_desenvolvimento
```

### 3. Configurar variáveis de ambiente (Produção)

```bash
# OBRIGATÓRIAS em produção
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://seu-host:5432/mentor_zen_prod
export DATABASE_USERNAME=seu_usuario
export DATABASE_PASSWORD=sua_senha_segura
# Gerar com: openssl rand -base64 32
export JWT_SECRET=sua_chave_jwt_super_segura_gerada_aleatoriamente_com_pelo_menos_256_bits

export JWT_ISSUER=mentor-redacao-zen
export JWT_EXPIRE_DURATION=7d
```

## Executar a aplicação

### Desenvolvimento
```bash
# Com profile dev (padrão)
mvn spring-boot:run

# Ou especificando o profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Produção
```bash
# Definir profile de produção
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
```

## Liquibase

O Liquibase executará automaticamente as migrações na inicialização da aplicação.

### Comandos úteis do Liquibase

```bash
# Ver status das migrações
mvn liquibase:status

# Aplicar migrações manualmente
mvn liquibase:update

# Rollback da última migração
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

## Usuários padrão

Após a primeira execução, os seguintes usuários estarão disponíveis:

### Admin
- **Email:** admin@mentorzen.com
- **Senha:** password
- **Role:** ADMIN

### Estudante Demo
- **Email:** joao@exemplo.com
- **Senha:** password
- **Role:** STUDENT

## Endpoints de Autenticação

### Login
```bash
POST /auth/login
Content-Type: application/json

{
  "email": "joao@exemplo.com",
  "password": "password"
}
```

### Registro
```bash
POST /auth/register
Content-Type: application/json

{
  "name": "Novo Usuário",
  "email": "novo@exemplo.com",
  "password": "minhasenha",
  "schoolGrade": "3º Ano",
  "studyGoals": "Passar no ENEM"
}
```

## Swagger UI

Acesse a documentação da API em:
- **Desenvolvimento:** http://localhost:8080/swagger-ui.html
- **Produção:** https://seu-dominio.com/swagger-ui.html

## Troubleshooting

### Erro de conexão com PostgreSQL
1. Verifique se o PostgreSQL está rodando
2. Confirme as credenciais e URL do banco
3. Verifique se o banco de dados existe

### Erro do Liquibase
1. Verifique se o usuário tem permissões para criar tabelas
2. Confirme se não há conflitos de schema
3. Use `mvn liquibase:status` para verificar o estado das migrações

### Erro de JWT
1. Verifique se a variável JWT_SECRET está definida
2. Confirme se o secret tem pelo menos 256 bits (32 caracteres)
3. Em produção, use um secret forte e único

## ⚠️ Segurança Crítica

### JWT Secret
- **NUNCA** commitar secrets no código
- **SEMPRE** usar variáveis de ambiente em produção
- **Gerar** secrets aleatórios: `openssl rand -base64 32`
- **Rotacionar** secrets periodicamente em produção

### Exemplo de geração de secret seguro:
```bash
# Gerar um JWT secret seguro
openssl rand -base64 32
```
