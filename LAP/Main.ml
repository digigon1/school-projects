open BTree

let read_cmd str =
	try String.sub str 0 (String.index str ' ') with
	| Not_found -> str
;;

let print_help () =
	print_string "Valid commands: exit, help, show, max\n"
;;

let get_filename str =
	let start = (String.index str ' ')+1 in
	String.sub str start ((String.length str)-start)
;;

let rec main () =
	let cmd = read_line () in 
	match (read_cmd cmd) with
	| "exit" -> ()
	| "help" -> print_help (); main ()
	| "show" -> show (load (get_filename cmd)); main ()
	| "max" -> print_int (max (load (get_filename cmd))); print_string "\n"; main ()
	| _ -> print_help (); main ()
;;

main ()