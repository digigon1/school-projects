/*ficha 4*/

select * from user_tables;
select * from alunos;

/*1.*/
select nome, trunc((sysdate-data_nsc)/365.2425) 
from alunos;

/*2.*/
select alunos.nome,cursos.nome
from alunos inner join cursos using(cod_curso);

/*3.*/
select alunos.nome
from alunos,docentes
where alunos.nome = docentes.nome;

/*4.*/
select cadeiras.nome
from cadeiras, alunos, inscricoes
where alunos.num_aluno = 1 and 
alunos.num_aluno = inscricoes.num_aluno and
inscricoes.cod_cadeira = cadeiras.cod_cadeira;

select cadeiras.nome
from cadeiras inner join inscricoes using(cod_cadeira), alunos
where alunos.num_aluno = 1 and
inscricoes.num_aluno = alunos.num_aluno;

/*5.*/
select distinct alunos.nome
from alunos, inscricoes, cadeiras, departamentos
where alunos.num_aluno = inscricoes.num_aluno and
inscricoes.cod_cadeira = cadeiras.cod_cadeira and
cadeiras.cod_departamento = departamentos.cod_departamento and
departamentos.nome = 'Departamento de Informatica';

select distinct alunos.nome
from alunos inner join inscricoes using(num_aluno) inner join cadeiras using(cod_cadeira) inner join departamentos using (cod_departamento)
where departamentos.nome like '%Informatica%';

/*6.*/
select alunos.nome, cadeiras.nome
from alunos, inscricoes, cursos, cadeiras, departamentos
where alunos.num_aluno = inscricoes.num_aluno and
inscricoes.cod_cadeira = cadeiras.cod_cadeira and
cadeiras.cod_departamento = departamentos.cod_departamento and
departamentos.nome like '%Informatica%' and
cursos.cod_curso = inscricoes.cod_curso and
cursos.nome not like '%Informatica%';

select alunos.nome as aluno, cadeiras.nome as cadeira, cursos.nome as curso
from alunos inner join cursos using(cod_curso) 
            inner join inscricoes using(num_aluno) 
            inner join cadeiras using(cod_cadeira) 
            inner join departamentos using(cod_departamento) 
where departamentos.nome like '%Informatica%' and
cursos.nome not like '%Informatica%'
order by aluno;

/*7. (not right)*/
select distinct alunos.nome
from alunos inner join inscricoes using(num_aluno);

/*9.*/

/*ficha 5*/
define idd=trunc((sysdate-data_nsc)/365.2425);

/*1*/
select avg(&idd)
from alunos;

/*2*/
select count(*)
from alunos;

/*3*//*if needed, use count*/
select distinct local
from alunos
where local is not null;

/*4*/
select sexo, count(*)
from alunos
group by sexo;

/*5*/
select &idd as idade, count(*)
from alunos
group by &idd;

/*6*/
select total, local
from (
select count(*) as total, alunos.local
from alunos
group by local
)
where total > 1;

/*7*/
select nome
from(
select nome, &idd as idade
from alunos
)
where idade = (
select max(&idd)
from alunos
);

/*8*/
with aluno_media as
(
select num_aluno, avg(nota) as media
from inscricoes
where nota is not null
group by num_aluno
) 
select nome, media
from alunos inner join aluno_media using(num_aluno);

/*9*/
with aluno_media as
(
select num_aluno, avg(nota) as media
from inscricoes
where nota is not null
group by num_aluno
),
nome_media as (
select nome, media
from alunos inner join aluno_media using(num_aluno)
)
select nome
from nome_media
where media > 12;

/*10*/
select categorias.nome as cat, docentes.nome as doc
from categorias left join docentes using(cod_categoria)
;

/*11*/
with cat_doc as (
select categorias.nome as cat, docentes.nome as doc
from categorias left join docentes using(cod_categoria)
)
select distinct cat
from cat_doc
where doc is null
;

/*12*/
select /*cadeiras.nome, cod_cadeira as cod, */cod_cadeira_p as prec, count(*) as quant
from precedencias inner join cadeiras using(cod_cadeira)
                  inner join cursos using(cod_curso)
where cursos.nome = 'Engenharia Informatica'
group by cod_cadeira_p
;

/*13*/
with cod_quant as (
select /*cadeiras.nome, cod_cadeira as cod, */cod_cadeira_p as cod_cadeira, count(*) as quant
from precedencias inner join cadeiras using(cod_cadeira)
                  inner join cursos using(cod_curso)
where cursos.nome = 'Engenharia Informatica'
group by cod_cadeira_p
)
select nome, quant
from cadeiras inner join cod_quant using(cod_cadeira)
;

/*14*/

/*19*/
select inscricoes.num_aluno, inscricoes.nota, cadeiras.nome, inscricoes.data_avaliacao
from inscricoes inner join cadeiras using(cod_cadeira)
                inner join departamentos using(cod_departamento)
where data_avaliacao<=to_date('31-12-2014','DD-MM-YYYY')
  and data_avaliacao>=to_date('01-01-2014','DD-MM-YYYY')
  and departamentos.nome like '%Infor%';