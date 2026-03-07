# language: pt
Funcionalidade: Autenticacao de usuario
  Como consumidor do microsservico de autenticacao
  Eu quero autenticar com credenciais validas
  Para receber um token de acesso

  Cenario: Autenticacao bem-sucedida
    Dado que existe um usuario cadastrado com email "usuario@fiap.com" e senha "Senha@123"
    Quando eu envio uma requisicao de autenticacao com email "usuario@fiap.com" e senha "Senha@123"
    Entao o status da resposta deve ser 200
    E a resposta deve conter um token JWT valido

  Cenario: Falha de autenticacao com senha invalida
    Dado que existe um usuario cadastrado com email "usuario@fiap.com" e senha "Senha@123"
    Quando eu envio uma requisicao de autenticacao com email "usuario@fiap.com" e senha "SenhaInvalida"
    Entao o status da resposta deve ser 401
    E a resposta deve conter a mensagem "E-mail ou senha inválidos"
