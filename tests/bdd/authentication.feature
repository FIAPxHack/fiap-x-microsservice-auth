Funcionalidade: Autenticação de usuário
  Como consumidor do microsserviço de autenticação
  Eu quero autenticar com credenciais válidas
  Para receber um token de acesso

  Cenário: Autenticação bem-sucedida
    Dado que existe um usuário cadastrado com email "usuario@fiap.com" e senha "Senha@123"
    Quando eu envio uma requisição de autenticação com email "usuario@fiap.com" e senha "Senha@123"
    Então o status da resposta deve ser 200
    E a resposta deve conter um token JWT válido

  Cenário: Falha de autenticação com senha inválida
    Dado que existe um usuário cadastrado com email "usuario@fiap.com" e senha "Senha@123"
    Quando eu envio uma requisição de autenticação com email "usuario@fiap.com" e senha "SenhaInvalida"
    Então o status da resposta deve ser 401
    E a resposta deve conter a mensagem "Credenciais inválidas"
