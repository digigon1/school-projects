accao(nome : go(X,Y),
 condicoes : [at(X)], 
   efeitos : [at(Y),-at(X)], 
restricoes : [(X\==Y)]).
 
accao(nome : buy(X),
 condicoes : [sell(L,X),at(L)],
   efeitos : [have(X)], 
restricoes : []).
 
inicial([at(home),sell(super,banana),sell(hws,drill),sell(super,milk)]).
 
objectivos([have(milk),have(drill),have(banana),at(home)]).

%Verifica se todos os elementos de uma lista s�o membros de outra (OK)
checkMembers([],_).
checkMembers([X|Xs],I) :- member(X,I), checkMembers(Xs,I).

%Verifica se um estado Fn � objetivo (OK)
satisfiedGoal(Fn) :- objectivos(X), checkMembers(X,Fn).

%verifica se as vari�veis respeitam todas as restri��es (OK)
%ground(C) -> verifica se a vari�vel C est� instanciada
checkConstraints([]).
checkConstraints([C|Cs]) :- C, checkConstraints(Cs).
%checkConstraints([X\==Y|Cs]) :- ground(X\==Y), X\==Y, checkConstraints(Cs).

%aplica os efeitos ao estado I e guarda a lista obtida em Res (OK)
applyEffects([],Res,Res).
applyEffects([-E|Es],I,Res) :- !,select(E,I,I1),applyEffects(Es,I1,Res).%delete(I,E,I1), applyEffects(Es,I1,Res).
applyEffects([E|Es],I,Res) :- !,applyEffects(Es,[E|I],Res).
applyEffects([_|Es],I,Res) :- applyEffects(Es,I,Res).

%testa um plano fornecido (OK)
testPlan([],Fn,[],Fn).
testPlan([P|Ps],I,[P,P2|Exec],Fn) :-
accao(nome : P, condicoes : LC, efeitos : LE, restricoes : LR), %verifica se a ac��o P � v�lida e obt�m LC (lista de condi��es), LE (lista de efeitos) e LR (lista de restri��es)
checkMembers(LC,I), %verifica se as condi��es (LC) s�o respeitadas e ao mesmo tempo instancia vari�veis de acordo com estado inicial (I)
checkConstraints(LR),
applyEffects(LE,I,P2),
testPlan(Ps,P2,Exec,Fn).

%inicia o teste ao plano
testPlan(Plan,[I|Exec]) :- inicial(I), testPlan(Plan,I,Exec,Fn), satisfiedGoal(Fn).

%escreve a situa��o inicial
%nl = new line
writeExec([I|Exec]) :- write("Initial situation: "), write(I), nl, writeExecAux(Exec).

%escreve o resto das a��es at� ao objetivo
writeExecAux([]) :- write("Goal: "), objectivos(I), write(I), write(" satisfied").
writeExecAux([X,I|Exec]) :- write("Action perfomed: "), write(X), nl, write("Situation: "), write(I), nl, writeExecAux(Exec).

%inicia o testo do plano e mostra o resultado de cada passo
planExecute(P) :- testPlan(P,E), writeExec(E).

fp(S,G,P) :- fp(S,G,[],R), reverse(R,P).
fp(S,G,P,P) :- checkMembers(G,S).
fp(S,G,Os,P) :-
 accao(nome : O, condicoes : Pre, efeitos : E, restricoes : Ct),
 checkMembers(Pre,S),
 checkConstraints(Ct),   
 \+ member(O,Os),
 applyEffects(E,S,S1),
 fp(S1,G,[O|Os],P).

plano(Plan) :- inicial(I), objectivos(G), fp(I,G,Plan).