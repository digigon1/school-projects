type tree = Nil | Node of int * tree * tree ;;

let rec make w =
	match w with
	| [] -> Nil
	| x::xs -> Node (x, Nil, (make xs))
;;

let rec max t =
	match t with
	| Nil -> 0
	| Node (x, Nil, Nil) -> x
	| Node (x, Nil, r) -> Pervasives.max x (max r)
	| Node (x, l, Nil) -> Pervasives.max x (max l)
	| Node (x, l, r) -> Pervasives.max x (Pervasives.max (max r) (max l))
;;

let rec rev t =
	match t with
	| Nil -> Nil
	| Node (x, l, r) -> Node (x, rev r, rev l)
;;

let rec load_from_file ch =
	let line = (input_line ch) in
	if line = "-" then Nil else Node (int_of_string line, load_from_file ch, load_from_file ch)
;;

let load file =
	rev (load_from_file (open_in file))
;;

let t = load "test";;

let rec store_in_file ch t =
	match t with
	| Nil -> output_string ch "-\n"
	| Node (x, l, r) -> 
		output_string ch ((string_of_int x)^"\n");
		store_in_file ch l;
		store_in_file ch r
;;

let store file t =
	let ch = (open_out file) in (
	store_in_file ch t;
	flush ch;
	close_out ch
	)
;;

let rec repeat_str s n =
	if n=0 then "" else s^(repeat_str s (n-1))
;;

let rec show_aux t n =
	match t with
	| Nil -> output_string stdout ((repeat_str "  " n)^"-"^"\n")
	| Node (x, l, r) -> (
		output_string stdout ((repeat_str "  " n)^(string_of_int x)^"\n");
		if (not (l = Nil && r = Nil)) then (
			show_aux l (n+1);
			show_aux r (n+1)
		)
	)
;;

let show t =
	show_aux t 0
;;