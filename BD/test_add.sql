insert into Pessoa values('testID1', '111-111-1111', 'M', 'testContr', 'testAddress', 'Adriano', to_date('10-10-1995','DD-MM-YYYY'), null);
insert into Pessoa values('testID2', '112-112-1112', 'M', 'testContr', 'testAddress', 'Guilherme', to_date('10-10-1995','DD-MM-YYYY'), null);
insert into Pessoa values('testID3', '113-113-1113', 'F', 'testContr', 'testAddress', 'Catarina', to_date('10-10-1995','DD-MM-YYYY'), 'is dead');
insert into Pessoa values('testID4', '114-114-1114', 'F', 'testContr', 'testAddress', 'Fernanda', to_date('10-10-1995','DD-MM-YYYY'), null);
insert into Pessoa values('testID5', '115-115-1115', 'F', 'testContr', 'testAddress', 'Swaggy', to_date('10-10-1995','DD-MM-YYYY'), null);

insert into Pessoa values('testIDWrong1', '111-111-1111', 'N', 'testContr', 'testAddress', 'Adriano', to_date('10-10-1995','DD-MM-YYYY'), null);
insert into Pessoa values('testIDWrong2', '111-111-1111', 'M', null, 'testAddress', 'Adriano', to_date('10-10-1995','DD-MM-YYYY'), null);
insert into Pessoa values('testIDWrong3', '111-111-1111', 'M', 'testContr', null, 'Adriano', to_date('10-10-1995','DD-MM-YYYY'), null);


insert into Funcionario values('testID1', 1000, 'segSocial1');
insert into Funcionario values('testID2', 3000.5, 'segSocial2');


insert into Inscricao values(seq_inscricao.nextval, to_date('10-10-1995','DD-MM-YYYY'), 25.30, 1);
insert into Inscricao values(seq_inscricao.nextval, to_date('10-10-1995','DD-MM-YYYY'), 30, 0);
insert into Inscricao values(seq_inscricao.nextval, to_date('10-10-1995','DD-MM-YYYY'), 20, 0);

insert into NaoLivre values(1, 7);
insert into NaoLivre values(2, 2);
insert into Livre values(3);

insert into Utilizador values('testID3', 1, 1);
insert into Utilizador values('testID4', 0, 2);
insert into Utilizador values('testID5', 0, 3);

select nome from Pessoa;
select * from Funcionario;
select * from Inscricao;
select * from NaoLivre;
select * from Livre;
select * from Utilizador;