Para correr o ficheiro MotionTest.jar, é necessário ter na mesma localização uma pasta motion que contenha o ficheiro th.png correspondente ao terreno.
Ou seja, a estrutura de ficheiros precisa de estar assim:

folder
|	MotionTest.jar
|	motion
	|	th.png


O comando para correr o ficheiro é: java -jar MotionTest.jar

O ficheiro vai compilado com a heurística da distância euclidiana, os pontos i=11 e j=2 e ANIMATE=true, sendo que houve uma alteração ligeira ao funcionamento dessa classe: para se perceber melhor o que está a acontecer, os pontos são pintados de rosa e azul alternadamente.