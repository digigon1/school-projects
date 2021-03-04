drop table Pessoa cascade constraints;
create table Pessoa(
  identidade VARCHAR(20) not null,
  contacto VARCHAR(20) not null,
  sexo CHARACTER(1) not null CHECK (sexo in ('F', 'M')),
  contribuinte VARCHAR(20) not null,
  morada VARCHAR(70) not null,
  nome VARCHAR(70) not null,
  data_nascimento DATE not null,
  observacoes VARCHAR(200),
  primary key (identidade)
);

drop table Funcionario cascade constraints;
create table Funcionario(
  identidade VARCHAR(20) not null,
  salario DECIMAL(10,2) not null,
  seguranca_social VARCHAR(20) not null,
  primary key (identidade),
  foreign key (identidade) references Pessoa(identidade)
);

drop table Tecnico cascade constraints;
create table Tecnico(
  identidade VARCHAR(20) not null,
  primary key (identidade),
  foreign key (identidade) references Funcionario(identidade)
);


drop table Treinador cascade constraints;
create table Treinador(
  identidade VARCHAR(20) not null,
  primary key (identidade),
  foreign key (identidade) references Funcionario(identidade)
);

drop table Inscricao cascade constraints;
create table Inscricao(
  cod_inscricao NUMBER(10) not null,
  data_inscricao DATE not null,
  mensalidade NUMERIC(4,2) not null,
  promocao CHARACTER(1) not null CHECK (promocao in ('S', 'N')),
  primary key (cod_inscricao)
);

drop table Utilizador cascade constraints;
create table Utilizador(
  identidade VARCHAR2(20) not null,
  prob_saude CHARACTER(1) not null CHECK (prob_saude in ('S', 'N')),
  cod_inscricao NUMBER(10) not null,
  primary key (identidade),
  foreign key (cod_inscricao) references Inscricao(cod_inscricao),
  foreign key (identidade) references Pessoa (identidade)
);

drop table Maquina cascade constraints;
create table Maquina(
  id_maquina NUMBER(10) not null,
  modelo VARCHAR(20) not null,
  nome VARCHAR(50) not null,
  identidade VARCHAR2(20) not null,
  primary key (id_maquina),
  foreign key (identidade) references Tecnico(identidade)
);

drop table NaoLivre cascade constraints;
create table NaoLivre(
  cod_inscricao NUMBER(10) not null,
  dias NUMBER(1) CHECK (0 < dias and dias < 8),
  primary key (cod_inscricao),
  foreign key (cod_inscricao) references Inscricao(cod_inscricao)
);

drop table Livre cascade constraints;
create table Livre(
  cod_inscricao NUMBER(10) not null,
  primary key (cod_inscricao),
  foreign key (cod_inscricao) references Inscricao(cod_inscricao)
);

drop table Rotina cascade constraints;
create table Rotina(
  cod_inscricao NUMBER(10) not null,
  dia_semana NUMBER(1) not null CHECK (0 < dia_semana and dia_semana < 8),
  primary key (cod_inscricao, dia_semana),
  foreign key (cod_inscricao) references NaoLivre(cod_inscricao)
);


drop table Exercicio cascade constraints;
create table Exercicio(
  codigo NUMBER(15) not null,
  nome VARCHAR(50) not null,
  primary key (codigo)
);

drop table Usa cascade constraints;
create table Usa(
  codigo NUMBER(15) not null,
  id_maquina NUMBER(10) not null,
  primary key (codigo, id_maquina),
  foreign key (codigo) references Exercicio(codigo),
  foreign key (id_maquina) references Maquina(id_maquina)
);

drop table Faz cascade constraints;
create table Faz(
  cod_inscricao NUMBER(10) not null,
  dia_semana NUMBER(1) not null,
  codigo NUMBER(15) not null,
  peso NUMERIC(6,2),
  repeticoes NUMBER(3),
  primary key (cod_inscricao, dia_semana, codigo),
  foreign key (cod_inscricao, dia_semana) references Rotina(cod_inscricao, dia_semana),
  foreign key (codigo) references Exercicio(codigo)
);

drop table Treina cascade constraints;
create table Treina(
  identidade VARCHAR(20) not null,
  cod_inscricao NUMBER(10) not null,
  primary key (identidade, cod_inscricao),
  foreign key (identidade) references Treinador(identidade),
  foreign key (cod_inscricao) references NaoLivre(cod_inscricao)
);

drop sequence seq_inscricao;
create sequence seq_inscricao start with 1 increment by 1;

drop sequence seq_exer;
create sequence seq_exer start with 1 increment by 1;

drop sequence seq_maquina;
create sequence seq_maquina start with 1 increment by 1;

/* Codigo de inscricao sequencial */
create or replace trigger ins_seq before insert on Inscricao
referencing new as nrow
for each row
declare
	next_ins number;
begin
	select seq_inscricao.nextval into next_ins from dual;
	:nrow.cod_inscricao := next_ins;
end;
/
/* Codigo do exercicio sequencial */
create or replace trigger exer_seq before insert on Exercicio
referencing new as nrow
for each row
declare
	next_exer number;
begin
	select seq_exer.nextval into next_exer from dual;
	:nrow.codigo := next_exer;
end;
/
/* Codigo da maquina sequencial */
create or replace trigger maq_seq before insert on Maquina
referencing new as nrow
for each row
declare
	next_maq number;
begin
	select seq_maquina.nextval into next_maq from dual;
	:nrow.id_maquina := next_maq;
end;
/

/* tecnico nao pode ser treinador */
create or replace trigger not_trein before insert on Tecnico
	referencing new as nrow
	for each row
	declare
		t_count number;
	begin
		select count(*) into t_count 
		from Treinador 
		where Treinador.identidade=:nrow.identidade;

		if(t_count > 0) then
			RAISE_APPLICATION_ERROR(-20000,'Funcionario nao pode ter dois trabalhos');
		end if;
	end;
	/

/* treinador nao pode ser tecnico */
create or replace trigger not_tecn before insert on Treinador
	referencing new as nrow
	for each row
	declare
		t_count number;
	begin
		select count(*) into t_count 
		from Tecnico 
		where Tecnico.identidade=:nrow.identidade;

		if(t_count > 0) then
			RAISE_APPLICATION_ERROR(-20000,'Funcionario nao pode ter dois trabalhos');
		end if;
	end;
	/

/* inscricao apenas livre */
create or replace trigger not_not_free before insert on Livre
	referencing new as nrow
	for each row
	declare
		t_count number;
	begin
		select count(*) into t_count
		from NaoLivre
		where NaoLivre.cod_inscricao=:nrow.cod_inscricao;

		if(t_count > 0) then
			RAISE_APPLICATION_ERROR(-20001,'Inscricao nao pode ser de dois tipos');
		end if;
	end;
	/

/* inscricao apenas nao-livre */
create or replace trigger not_free before insert on NaoLivre
	referencing new as nrow
	for each row
	declare
		t_count number;
	begin
		select count(*) into t_count
		from Livre
		where Livre.cod_inscricao=:nrow.cod_inscricao;

		if(t_count > 0) then
			RAISE_APPLICATION_ERROR(-20001,'Inscricao nao pode ser de dois tipos');
		end if;
	end;
	/

/* View dos funcionarios */
create or replace view v_func as
select 
"NOME",
"SEXO",
"MORADA",
"CONTRIBUINTE",
"DATA_NASCIMENTO",
"CONTACTO",
"SALARIO",
"SEGURANCA_SOCIAL",
"OBSERVACOES",
"IDENTIDADE"
 from   "FUNCIONARIO" inner join "PESSOA" using("IDENTIDADE");

create or replace trigger up_func instead of update on v_func
	for each row
	begin
		update PESSOA
		set contacto = :NEW.contacto,
		sexo = :NEW.sexo,
		contribuinte = :NEW.contribuinte,
		morada = :NEW.morada,
		nome = :NEW.nome,
		data_nascimento = :NEW.data_nascimento,
		observacoes = :NEW.observacoes
		where identidade = :NEW.identidade;

		update FUNCIONARIO
		set salario = :NEW.salario,
		seguranca_social = :NEW.seguranca_social
		where identidade = :NEW.identidade;
	end;
	/

create or replace trigger ins_func instead of insert on v_func
	for each row
	declare
		num number;
	begin
		select count(*) into num 
		from Pessoa
		where identidade = :NEW.identidade;

		if(num = 0) then
			insert into Pessoa values (:NEW.identidade,:NEW.contacto,:NEW.sexo,:NEW.contribuinte,:NEW.morada,:NEW.nome,:NEW.data_nascimento,:NEW.observacoes);
		end if;
		insert into FUNCIONARIO values (:NEW.identidade,:NEW.salario,:NEW.seguranca_social);
	end;
	/

create or replace trigger delete_func instead of delete on v_func
	for each row
	declare 
		user_num number;
	begin
		delete from FUNCIONARIO where identidade = :OLD.identidade;
		
		select count(*) into user_num 
		from UTILIZADOR 
		where identidade = :OLD.identidade;

		if(user_num = 0) then
			delete from PESSOA where identidade = :OLD.identidade;
		end if;
	end;
	/

/* View dos utilizadores */
create or replace view v_user as
select 
"IDENTIDADE",
"NOME",
"SEXO",
"MORADA",
"CONTRIBUINTE",
"DATA_NASCIMENTO",
"CONTACTO",
"PROB_SAUDE",
"OBSERVACOES",
"COD_INSCRICAO"
 from   "UTILIZADOR" inner join "PESSOA" using("IDENTIDADE");

create or replace trigger up_user instead of update on v_user
	for each row
	begin
		update PESSOA
		set contacto = :NEW.contacto,
		sexo = :NEW.sexo,
		contribuinte = :NEW.contribuinte,
		morada = :NEW.morada,
		nome = :NEW.nome,
		data_nascimento = :NEW.data_nascimento,
		observacoes = :NEW.observacoes
		where identidade = :NEW.identidade;

		update UTILIZADOR
		set prob_saude = :NEW.prob_saude,
		cod_inscricao = :NEW.cod_inscricao
		where identidade = :NEW.identidade;
	end;
	/

create or replace trigger ins_user instead of insert on v_user
	for each row
	declare
		num number;
		dias number;
		insc number;
	begin
		select count(*) into num 
		from Pessoa
		where identidade = :NEW.identidade;

		if(num = 0) then
			insert into Pessoa values (:NEW.identidade,:NEW.contacto,:NEW.sexo,:NEW.contribuinte,:NEW.morada,:NEW.nome,:NEW.data_nascimento,:NEW.observacoes);
		end if;

		insert into Utilizador values (:NEW.identidade,:NEW.prob_saude, seq_inscricao.currval);

	end;
	/

create or replace trigger delete_user instead of delete on v_user
	for each row
	declare
		fun_num number;
	begin
		delete from Utilizador where identidade = :OLD.identidade;
		
		select count(*) into fun_num 
		from FUNCIONARIO 
		where identidade = :OLD.identidade;

		if(fun_num = 0) then
			delete from PESSOA where identidade = :OLD.identidade;
		end if;
	end;
	/

create or replace view v_treino as
select
ROWID pk,
identidade,
cod_inscricao
from Treina;

create or replace trigger up_treino instead of update on v_treino
	for each row
	begin
	update Treina
		set cod_inscricao = :NEW.cod_inscricao,
		identidade = :NEW.identidade
		where ROWID = :NEW.pk;
	end;
	/

create or replace trigger ins_treino instead of insert on v_treino
	for each row
	begin
	insert into Treina values (:NEW.identidade, :NEW.cod_inscricao);
	end;
	/

create or replace trigger del_treino instead of delete on v_treino
	for each row
	begin
	delete from Treina where ROWID = :OLD.pk;
	end;
	/

create or replace view v_faz as
select ROWID pk,
cod_inscricao,
dia_semana,
codigo,
peso,
repeticoes
from Faz;

create or replace trigger up_faz instead of update on v_faz
	for each row
	begin
	update Faz
		set cod_inscricao = :NEW.cod_inscricao,
		dia_semana = :NEW.dia_semana,
		codigo = :NEW.codigo,
		peso = :NEW.peso,
		repeticoes = :NEW.repeticoes
		where ROWID = :NEW.pk;
	end;
	/

create or replace trigger ins_faz instead of insert on v_faz
	for each row
	begin
	insert into Faz values (:NEW.cod_inscricao, :NEW.dia_semana, :NEW.codigo, :NEW.peso, :NEW.repeticoes);
	end;
	/

create or replace trigger del_faz instead of delete on v_faz
	for each row
	begin
	delete from Faz where ROWID = :OLD.pk;
	end;
	/

create or replace trigger num_max_rotina before insert on Rotina
	for each row
	declare
		num number;
		num_max number;
	begin
		select count(*) into num
		from Rotina
		where cod_inscricao = :NEW.cod_inscricao;

		select dias into num_max
		from NaoLivre
		where cod_inscricao = :NEW.cod_inscricao;

		if(num = num_max) then
			RAISE_APPLICATION_ERROR(-20003,'Utilizador nao se pode inscrever em mais dias sem aumentar o numero de dias em que pode vir');
		end if;
	end;
	/

create or replace view v_usa as
select ROWID pk,
codigo,
id_maquina
from Usa;

create or replace trigger up_usa instead of update on v_usa
	for each row
	begin
		update Usa
			set codigo = :NEW.codigo,
			id_maquina = :NEW.id_maquina
			where ROWID = :NEW.pk;
	end;
	/

create or replace trigger ins_usa instead of insert on v_usa
	for each row
	begin
		insert into Usa values (:NEW.codigo, :NEW.id_maquina);
	end;
	/

create or replace trigger del_usa instead of delete on v_usa
	for each row
	begin
		delete from Usa where ROWID = :OLD.pk;
	end;
	/




/* INSERCAO DE VALORES */
insert into Pessoa values ('id1','919191','M','cont1','rua dos passarinhos','joao',to_date('23-10-1995','DD-MM-YYYY'),null);
insert into Pessoa values ('id2','929292','F','cont2','rua dos piu-pius','joana',to_date('23-10-1995','DD-MM-YYYY'),null);
insert into Pessoa values ('id3','939393','F','cont3','avenida principal n3','catarina',to_date('23-10-1995','DD-MM-YYYY'),'isto e uma observacao de exemplo');
insert into Pessoa values ('id4','949494','F','cont4','avenida de jesus 2442-312','dina',to_date('23-10-1995','DD-MM-YYYY'),'esta sera a segunda e unica outra observacao');
insert into Pessoa values ('id5','959595','F','cont5','praca da brincadeira','carolina',to_date('23-10-1995','DD-MM-YYYY'),null);
insert into Pessoa values ('id6','969696','F','cont6','avenida rua','monica',to_date('23-10-1995','DD-MM-YYYY'),null);
insert into Pessoa values ('id7','979797','F','cont7','rua avenida','lisa',to_date('23-10-1995','DD-MM-YYYY'),null);
insert into Pessoa values ('id8','989898','M','cont8','rua ronaldo','goncalo',to_date('10-09-1995','DD-MM-YYYY'),null);
insert into Pessoa values ('id9','999999','M','cont9','praceta joao jesus','keivin',to_date('23-10-1995','DD-MM-YYYY'),null);
insert into Pessoa values ('id10','313-909090','M','cont100','rua do globo','joao miguel',to_date('23-10-1995','DD-MM-YYYY'),'doenca infecciosa');
commit;
insert into Pessoa values ('iddemasiadolongopropositadamenteistochega','a','M','cont','addr','nome',sysdate,null);
insert into Pessoa values ('id','9','S','cont','addr','name',sysdate,'sexo errado');
insert into Pessoa values ('id',null,'F','cont','addr','name',sysdate,'testar not nulls');
insert into Pessoa values ('id1','9','F','cont','addr','name',sysdate,'primary key repetida');
commit;

insert into Funcionario values ('id1',234.00,'ss1');
insert into Funcionario values ('id2',234.56,'ss2');
insert into Funcionario values ('id4',123.45,'ss4');
insert into Funcionario values ('id10',600.00,'ss10');
commit;
insert into Funcionario values ('id1',234.00,'ss10');
insert into Funcionario values ('iderrado',234.00,'ss1');
insert into Funcionario values ('id1',234.00,null);
commit;

insert into Tecnico values ('id1');
insert into Tecnico values ('id2');
commit;
insert into Tecnico values ('iderrado');
insert into Tecnico values ('id3');
commit;

insert into Treinador values ('id4');
insert into Treinador values ('id10');
commit;

insert into Tecnico values ('id4');
insert into Treinador values ('iderrado');
insert into Treinador values ('id1');
commit;

insert into Maquina values (0,'R-135','rower','id1');
insert into Maquina values (0,'B-531','bike','id2');
insert into Maquina values (0,'T-331','threadmill','id2');
commit;

insert into Maquina values (0, 'wrong','tech','id4');
commit;

insert into Inscricao values (0,sysdate,20.00,'S');
insert into Inscricao values (0,sysdate,23.40,'N');
insert into Inscricao values (0,sysdate,23.40,'N');
insert into Inscricao values (0,sysdate,29.99,'S');
insert into Inscricao values (0,sysdate,34.99,'S');
insert into Inscricao values (0,sysdate,40.00,'N');
insert into Inscricao values (0,sysdate,50.00,'N');
commit;
insert into Inscricao values (0,sysdate,00.00,'F');
commit;

insert into Livre values (1);
insert into Livre values (2);
insert into Livre values (3);
commit;
insert into Livre values (10);
commit;

insert into NaoLivre values (4,2);
insert into NaoLivre values (5,3);
insert into NaoLivre values (6,3);
insert into NaoLivre values (7,4);
commit;
insert into Livre values (4);
insert into NaoLivre values (2,3);
commit;

insert into Utilizador values ('id5','N',1);
insert into Utilizador values ('id10','S',2);
insert into Utilizador values ('id3','N',3);
insert into Utilizador values ('id6','N',4);
insert into Utilizador values ('id7','N',5);
insert into Utilizador values ('id8','N',6);
insert into Utilizador values ('id9','N',7);
commit;
insert into Utilizador values ('id5','N',3);
commit;

insert into Treina values ('id10',4);
insert into Treina values ('id10',5);
insert into Treina values ('id4',7);
commit;
insert into Treina values ('id3',4);
insert into Treina values ('id10',1);
commit;

insert into Exercicio values (0,'Remar');
insert into Exercicio values (0,'Sprint');
insert into Exercicio values (0,'Maratona');
commit;
insert into Exercicio values (0,null);
commit;

insert into Usa values (1,1);
insert into Usa values (2,2);
insert into Usa values (3,2);
insert into Usa values (2,3);
insert into Usa values (3,3);
commit;
insert into Usa values (4,4);
commit;

insert into Rotina values (4,1);
insert into Rotina values (4,2);
insert into Rotina values (5,1);
insert into Rotina values (6,2);
insert into Rotina values (7,1);
insert into Rotina values (7,4);
commit;
insert into Rotina values (4,4);
insert into Rotina values (1,5);
insert into Rotina values (6,8);
commit;

insert into Faz values (4,1,1,null,200);
insert into Faz values (4,1,2,null,null);
insert into Faz values (4,2,3,null,10);
insert into Faz values (5,1,1,null,null);
insert into Faz values (6,2,2,null,null);
insert into Faz values (7,1,3,null,20);
insert into Faz values (7,4,2,null,5);
commit;
insert into Faz values (5,2,1,null,null);
insert into Faz values (7,4,4,null,null);
commit;
