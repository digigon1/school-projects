let rec fold_left f a w =
	match w with
	| [] -> a
	| h::t -> fold_left f (f a h) t
;;

let rec fold_right f w a =
	match w with
	| [] -> a
	| h::t -> fold_right f t (f h a)
;;

let rec rev w =
	match w with
	| [] -> []
	| a::[] -> [a]
	| x::xs -> (rev xs)@[x]
;;

let rec map f w =
	match w with
	| [] -> []
	| h::t -> rev (fold_right (fun a l -> ((f a)::l)) w [])
;;

(* map f [1] -> (f 1)::(map f []) *)