let rec succAll w = 

	match w with
	| [] -> []
	| a::ws -> (a+1)::(succAll ws)

;;