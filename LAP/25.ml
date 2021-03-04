let rec fall tree =
	match tree with
	| Nil -> Nil
	| Node (a, left, right) when left = Nil && right = Nil -> Nil
	| Node (a, left, right) -> Node (a, fall left, fall right)
;;
