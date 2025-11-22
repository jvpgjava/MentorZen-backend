# Collection Postman - Mentor de Redação Zen API

Este documento explica como usar a collection do Postman para testar a API do Mentor de Redação Zen.

## Arquivos Incluídos

- `MentorZen_API.postman_collection.json` - Collection completa com todos os endpoints
- `MentorZen_API.postman_environment.json` - Arquivo de environment para configuração

## Como Importar

### 1. Importar a Collection

1. Abra o Postman
2. Clique em **Import** no canto superior esquerdo
3. Selecione o arquivo `MentorZen_API.postman_collection.json`
4. Clique em **Import**

### 2. Importar o Environment (Opcional mas Recomendado)

1. Clique em **Import** novamente
2. Selecione o arquivo `MentorZen_API.postman_environment.json`
3. Clique em **Import**
4. No canto superior direito, selecione o environment **"Mentor Zen - Local Development"**

### 3. Configurar Variáveis

Se você não importou o environment, configure manualmente:

1. Clique no ícone de **engrenagem** (⚙️) no canto superior direito
2. Selecione **"Mentor Zen - Local Development"** ou crie um novo environment
3. Configure as variáveis:
   - `base_url`: `http://localhost:8080` (ou a URL do seu servidor)
   - `token`: Deixe vazio inicialmente (será preenchido automaticamente após login)
   - `userId`: Deixe vazio inicialmente (será preenchido automaticamente após login)

## Como Usar

### Passo 1: Fazer Login

1. Abra a pasta **"Authentication"**
2. Execute a requisição **"Login"**
3. O token JWT será salvo automaticamente na variável `token`
4. Todas as outras requisições usarão este token automaticamente

**Exemplo de requisição de Login:**
```json
{
    "email": "joao@exemplo.com",
    "password": "password"
}
```

### Passo 2: Testar Endpoints Protegidos

Após o login, você pode testar qualquer endpoint protegido. O token será incluído automaticamente no header `Authorization: Bearer <token>`.

### Passo 3: Explorar a API

A collection está organizada em 4 pastas principais:

#### Authentication
- **Login**: Autentica e obtém token JWT
- **Register**: Registra novo usuário
- **Forgot Password**: Solicita recuperação de senha
- **Reset Password**: Redefine senha com token
- **Get Current User**: Obtém dados do usuário autenticado

#### Essays
- **Create Essay**: Cria nova redação em rascunho
- **Get All Essays**: Lista todas as redações (com paginação)
- **Get Essay by ID**: Busca redação específica
- **Update Essay**: Atualiza redação (apenas rascunhos)
- **Delete Essay**: Deleta redação (apenas rascunhos)
- **Submit Essay for Analysis**: Submete redação para análise com IA
- **Get Essays by Status**: Busca redações por status (DRAFT, SUBMITTED, ANALYZED, ARCHIVED)
- **Search Essays**: Busca redações por palavra-chave

#### Feedbacks
- **Get Essay Feedbacks**: Lista feedbacks de uma redação
- **Get Feedback by ID**: Busca feedback específico
- **Get User Feedbacks**: Lista todos os feedbacks do usuário
- **Get User Stats**: Estatísticas de desempenho do usuário

#### Profile
- **Get Profile**: Obtém perfil do usuário
- **Update Profile**: Atualiza informações do perfil
- **Change Password**: Altera senha
- **Upload Profile Picture**: Faz upload de foto de perfil
- **Delete Profile Picture**: Remove foto de perfil
- **Delete Account**: Deleta conta permanentemente

## Recursos da Collection

### Auto-save do Token

A requisição de **Login** possui um script de teste que salva automaticamente o token JWT na variável de ambiente `token`. Isso significa que você não precisa copiar e colar o token manualmente.

### Variáveis de Ambiente

A collection utiliza variáveis para facilitar a configuração:

- `{{base_url}}`: URL base da API (padrão: `http://localhost:8080`)
- `{{token}}`: Token JWT (preenchido automaticamente após login)
- `{{userId}}`: ID do usuário (preenchido automaticamente após login)

### Exemplos de Requisições

Todas as requisições incluem exemplos de dados válidos. Você pode modificar os dados conforme necessário para seus testes.

## Dados de Teste

### Usuário Padrão (se disponível no banco)

```
Email: joao@exemplo.com
Senha: password
```

### Exemplo de Redação

```json
{
    "title": "A Importância da Educação Digital no Brasil",
    "theme": "Com base na leitura dos textos motivadores...",
    "content": "A educação digital no Brasil representa..."
}
```

## Status de Redações

Os status possíveis são:

- `DRAFT`: Rascunho (pode ser editado/deletado)
- `SUBMITTED`: Submetida para análise
- `ANALYZED`: Analisada (com feedback disponível)
- `ARCHIVED`: Arquivada

## Troubleshooting

### Erro 401 (Unauthorized)

- Verifique se fez login e o token foi salvo
- Verifique se o token não expirou (tokens JWT têm validade de 7 dias por padrão)
- Faça login novamente se necessário

### Erro 404 (Not Found)

- Verifique se a URL está correta
- Verifique se o servidor está rodando
- Verifique se o ID do recurso existe

### Erro 400 (Bad Request)

- Verifique se os dados da requisição estão no formato correto
- Verifique se todos os campos obrigatórios foram preenchidos
- Consulte a documentação do Swagger para ver os campos obrigatórios

### Token não está sendo salvo

- Verifique se o script de teste na requisição de Login está habilitado
- Verifique se o environment está selecionado
- Execute a requisição de Login novamente

## Próximos Passos

1. Importe a collection e o environment
2. Faça login para obter o token
3. Explore os endpoints disponíveis
4. Teste diferentes cenários (criar, editar, deletar redações)
5. Teste o fluxo completo: criar redação → submeter → ver feedback

## Documentação Adicional

Para mais informações sobre a API, consulte:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/api-docs
- **README.md**: Documentação completa do backend

## Suporte

Se encontrar problemas ao usar a collection, verifique:

1. Se o servidor backend está rodando
2. Se as variáveis de ambiente estão configuradas corretamente
3. Se o banco de dados está configurado e acessível
4. Se as credenciais de login estão corretas

Para mais informações, consulte o arquivo `README.md` do backend.

