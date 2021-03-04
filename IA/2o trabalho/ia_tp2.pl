accao(nome : putTable(X),

 condicoes : [on(X,Z),clear(X)], 

   efeitos : [clear(Z),on(X,mesa),-on(X,Z)], 

restricoes : [(X\==Z)]).

 

accao(nome : putOn(X,Y),

 condicoes : [on(X,Z),clear(X),clear(Y)], 

   efeitos : [clear(Z),on(X,Y),-on(X,Z),-clear(Y)], 

restricoes : [(Y\==mesa),(X\==Y),(X\==Z),(Y\==Z)]).

 

 

inicial([clear(b),on(b,a),on(a,mesa),clear(d),on(d,c),on(c,mesa)]).

 

objectivos([on(a,c),on(b,d)]).

/*
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
*/



/*
accao(nome       : move(Room),
	  condicoes  : [robotAt(X), neighbour(X,Room)],  
      efeitos    : [-robotAt(X), robotAt(Room)],
      restricoes : [(X\==Room)]).

accao(nome       : grab(Object),
      condicoes  : [empty_handed(), objectAt(Object,Room), robotAt(Room)],
      efeitos    : [-empty_handed(), is_grabbing(Object), -objectAt(Object,Room)],
      restricoes : []).
	  
accao(nome       : drop(Object),
      condicoes  : [is_grabbing(Object), robotAt(Room)],
      efeitos    : [empty_handed(),-is_grabbing(Object),objectAt(Object,Room)],
      restricoes : []).

inicial([empty_handed(), robotAt(s1), objectAt(b1,s1), objectAt(b2,s2), objectAt(b3,s3),
		 neighbour(s1,s2), neighbour(s2,s1), neighbour(s2,s3), neighbour(s3,s2)]).

objectivos([objectAt(b1,s3), objectAt(b2,s3), objectAt(b3,s3)]).
*/

/* [grab(b1),move(s2),move(s3),drop(b1),move(s2),grab(b2),move(s3),drop(b2)] */

/**********************************************************************************************************************/


/*
accao(nome : calcarSap(Pe),
 condicoes : [meia(Pe)],
   efeitos : [sapato(Pe)],
restricoes : []).



accao(nome : calcarMeia(Pe),
 condicoes : [],
   efeitos : [meia(Pe)],
restricoes : []).

inicial([]).

objectivos([sapato(esq),sapato(dir)]).

*/



plano(P):-
	testPlan(TempP, _),
	length(TempP, N),
	!,
	testPlan(P, _),
	length(P, M),
	(M =< N; M > N, !, fail).


planExecute(P) :- testPlan(P,E), writeExec(E).

writeExec([Head|[Act|[Sit|Rest]]]):-
	!,
	format('Initial Situation: ~w\n', [Head]),
	writeRestExec(Act, Sit, Rest).

writeRestExec(Action, Situation, []):-
	!,
	format('Action performed: ~w\n', [Action]),
	format('Situation: ~w\n', [Situation]),
	objectivos(X),
	format('Goal ~w satisfied', [X]).
writeRestExec(Action, Situation, [Act|[Sit|Rest]]):-
	!,
	format('Action performed: ~w\n', [Action]),
	format('Situation: ~w\n', [Situation]),
	writeRestExec(Act, Sit, Rest).	


testPlan(Plan,[I|Exec]) :-
	inicial(I),
	testPlan(Plan,I,Exec,Fn,1),
	satisfiedGoal(Fn).

satisfiedGoal(List):-
	objectivos(X),
	checkCond(X, List).

testPlan(Plan, I, Exec, Fn, N):-
	testProgPlan(Plan, I, Exec, Fn, N);
	!,
	NewN is N+1,
	testPlan(Plan, I, Exec, Fn, NewN).

testProgPlan([],I,_,I,0).
testProgPlan([Head|Tail], I, [Head|[NewI|NewExec]], Fn, N):-
	N > 0,
	accao(nome:Head, condicoes:Cond, efeitos:Eff, restricoes:Rest),

	checkCond(Cond, I),
	runRest(Rest),

	effectFinal(Eff, I, NewI),

	NewN is N-1,
	testProgPlan(Tail, NewI, NewExec, Fn, NewN),
	satisfiedGoal(Fn).

runRest([]).
runRest([Head|Tail]):-
	Head,
	runRest(Tail).

checkCond([],_).
checkCond([Head|Tail], Fn):-
	member(Head, Fn),
	checkCond(Tail, Fn).

effectFinal([], Final, Final).
effectFinal([Head|Tail], Original, Final):-
	effectOneFinal(Head, Original, TempFinal),
	effectFinal(Tail, TempFinal, Final),


	sort(Original, SortOriginal),
	sort(Final, SortFinal),
	SortOriginal \== SortFinal.

effectOneFinal(-Effect, Original, Final):-
	!,
	select(Effect, Original, Final).
effectOneFinal(Effect, Original, Final):-
	list_to_set([Effect|Original],Final).
