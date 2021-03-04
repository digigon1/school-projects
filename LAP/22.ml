let rec height tree =
	match tree with
	| Nil -> 1
	| Node (a, left, right) -> 1 + max (height left) (height right)
;;

let rec balanced tree =
	match tree with
	| Nil -> true
	| Node (a, left, right) -> ((abs ((height left) - (height right))) < 2) && balanced left && balanced right
;;