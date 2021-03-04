let rec listElems w =
	match w with
	| [] -> []
	| h::t -> [h]::listElems t
;;

let rec joinElems w =
	match w with
	| [] -> []
	| h1::h2::t when (List.hd h1)=(List.hd h2) -> joinElems ([(h1@h2)]@t)
	| h1::h2::t -> h1::(joinElems ([h2]@t))
	| h::t -> [h]
;;

let rec countList w =
	match w with
	| [] -> 0
	| h::[] -> 1
	| h::t -> 1+countList t
;;

let rec count w =
	match w with
	| [] -> []
	| h::t -> (List.hd h, countList h)::(count t)
;;

let pack w =
	count (joinElems (listElems w))
;;

let rec unpack w =
	match w with
	| [] -> []
	| (x, 1)::xs -> x::(unpack xs)
	| (x, y)::xs -> x::(unpack ((x, y-1)::xs))
;;

joinElems [[1.2]; [1.2]; [1.2]; [1.3]; [1.3]; [1.2]; [1.3]];;
