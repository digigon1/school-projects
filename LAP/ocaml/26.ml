type 'a ntree = NNil | NNode of 'a * 'a ntree list;;

let rec map f list =
	match list with
	| [] -> []
	| h::t -> (f h)::(map f t)
;;

let rec flatten list =
	match list with
	| [] -> []
	| h::t -> List.hd h::flatten t
;;

let rec treeToList tree =
	match tree with
	| NNil -> []
	| NNode (a, list) -> a::(flatten (map treeToList list))
;;

let rec subtrees tree =
	match tree with
	| NNil -> [NNil]
	| NNode (a, list) -> tree::(flatten (map subtrees list))
;;

let rec spring v tree =
	match tree with
	| NNil -> NNode (v, [])
	| NNode (a, list) when list=[] -> NNode (a, [NNode (v, [])])
	| NNode (a, list) -> NNode (a, map (fun x -> spring v x) list)
;;

let rec filledWithNil list =
	match list with
	| [] -> true
	| h::t when h=NNil -> filledWithNil t
	| h::t -> false
;;

let rec fall tree =
	match tree with
	| NNil -> NNil
	| NNode (a, list) when list=[]-> NNil
	| NNode (a, list) when filledWithNil (map fall list) -> NNode (a, [])
	| NNode (a, list) -> NNode (a, map fall list)
;;

fall (NNode (1, [NNode (2, [NNode (10, []); NNode (10, [])]); NNode (3, [NNode (10, [])]); NNode (4, [NNode (10, [])])]));;