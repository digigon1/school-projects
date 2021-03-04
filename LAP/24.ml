let rec spring v tree =
	match tree with
	| Nil -> Node (v, Nil, Nil)
	| Node (a, left, right) -> Node (a, spring v left, spring v right)
;;