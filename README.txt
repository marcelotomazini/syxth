Para configurar o mockito como plugin do eclipse, execute

mvn p2:site -N

isso irá criar um repositorio p2 com mockito.

Vá em Window >> Preferences >> Plug-in Development >> Target Platform

Selecione Running Platform e clique em Edit
Cliquem em Add e adicione um Directory
Selecione o diretorio "${workspace_loc}/syxth/target/repository"

isso ira fazer com que o eclipse reconheça o mockito como um plugin interno
