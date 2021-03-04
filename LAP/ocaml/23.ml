let rec belongs elem w =
	match w with
	| [] -> false
	| head::tail -> head=elem || belongs elem tail
;;

let rec diff w1 w2 =
	match w1 with
	| [] -> []
	| head::tail -> if belongs head w2 then diff tail w2 else head::diff tail w2
;;

let union list1 list2 =
	(diff list1 list2)@list2
;;

let rec subtrees tree =
	match tree with
	| Nil ->[Nil]
	| Node (a, left, right) -> union (union [tree] (subtrees left)) (subtrees right)
;;