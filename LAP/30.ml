let rec countEmpty_aux ch =
	try (
		let line = input_line ch in
		(if line = "" then 1 else 0) + countEmpty_aux ch
	) with
	| End_of_file -> 0
;;

let countEmpty filename =
	countEmpty_aux (open_in filename)
;;

let rec clear_aux ch1 ch2 =
	try (
		let line = input_line ch1 in
		if line = "" then 1 + (clear_aux ch1 ch2) else (output_string ch2 (line^"\n"); clear_aux ch1 ch2)
	) with
	| End_of_file -> 0
;;

let clear input output =
	let i = (open_in input) in
	let o = (open_out output) in
	let value = clear_aux i o in 
	(
		flush o;
		close_out o;
		value
	)
;;
