type 'a tree = Nil | Node of 'a * 'a tree * 'a tree

let rec howMany v tree =
	match tree with
	| Nil -> 0
	| Node (a, left, right) when a=v -> 1 + (howMany v left) + (howMany v right)
	| Node (a, left, right) -> (howMany v left) + (howMany v right)
;;

let rec eqPairs tree =
	match tree with
	| Nil -> 0
	| Node ((a, b), left, right) when a=b -> 1 + (eqPairs left) + (eqPairs right)
	| Node ((a, b), left, right) -> (eqPairs left) + (eqPairs right)
;;

let rec treeToList tree =
	match tree with
	| Nil -> []
	| Node (a, left, right) -> (a::(treeToList left))@(treeToList right)
;;