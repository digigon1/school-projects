(* Geocaching module body *)

(* 
Aluno 1: 45353 Goncalo Almeida
Aluno 2: 45678 Jose Martins

Comentario:

?????????????????????????
?????????????????????????
?????????????????????????
?????????????????????????
?????????????????????????
?????????????????????????

*)


(*
01234567890123456789012345678901234567890123456789012345678901234567890123456789
   80 columns
*)

let altitudeUnknown = -32768 ;;

type cache = {          (* Example: *)
    code: string;       (* "GCK1JY" (url: http://coord.info/GCK1JY) *)
    name: string;       (* "Atlantis [Pico]" *)
    state: string;      (* "ARQUIPÉLAGO DOS AÇORES" *)
    owner: string;      (* "Joao&Olivia" *)
    latitude: float;    (* 38.468917 *)
    longitude: float;   (* -28.3994 *)
    kind: string;       (* "TRADITIONAL"  options: "MULTI", "PUZZLE", etc. *)
    size: string;       (* "REGULAR" options: "MICRO", "SMALL", "LARGE", etc. *)
    difficulty: float;  (* 2.0  options: 1.0, 1.5, 2.0, ..., 4.5, 5.0 *)
    terrain: float;     (* 4.5  options: 1.0, 1.5, 2.0, ..., 4.5, 5.0 *)
    status: string;     (* "AVAILABLE" options: "AVAILABLE", "DISABLED" *)
    hiddenDate: string; (* "2004/07/20" *)
    nFounds: int;       (* 196 *)
    nNotFounds: int;    (* 25 *)
    nFavourites: int;   (* 48 *)
    altitude: int       (* 2286  // -32768 significa altitude desconhecida *)
} ;;

let unknownCache = {
    code = ""; name = ""; state = ""; owner = "";
    latitude = 0.0; longitude = 0.0;
    kind = ""; size = ""; difficulty = 0.0; terrain = 0.0;
    status = ""; hiddenDate = "";
    nFounds = 0; nNotFounds = 0; nFavourites = 0;
    altitude = 0
} ;;

(* https://en.wikipedia.org/wiki/Haversine_formula *)
let haversine (lat1,lon1) (lat2,lon2) =
    let toRad deg = deg *. 3.1415926535898 /. 180.0 in
    let dLat = toRad (lat2 -. lat1) and dLon = toRad (lon2 -. lon1) in
    let sa = sin (dLat /. 2.0) and so = sin (dLon /. 2.0) in
    let a = sa *. sa +. so *. so *. cos(toRad lat1) *. cos(toRad lat2) in
        6372.8 *. 2.0 *. asin (sqrt a)
;;

let cacheDistance c1 c2 =
    haversine (c1.latitude, c1.longitude) (c2.latitude, c2.longitude)
;;

let loadCache ci =
    let code = input_line ci in
    let name = input_line ci in
    let state = input_line ci in
    let owner = input_line ci in
    let latitude = float_of_string (input_line ci) in
    let longitude = float_of_string (input_line ci) in
    let kind = input_line ci in
    let size = input_line ci in
    let difficulty = float_of_string (input_line ci) in
    let terrain = float_of_string (input_line ci) in
    let status = input_line ci in
    let hiddenDate = input_line ci in
    let nFounds = int_of_string (input_line ci) in
    let nNotFounds = int_of_string (input_line ci) in
    let nFavourites = int_of_string (input_line ci) in
    let altitude = int_of_string (input_line ci) in {
        code = code; name = name; state = state; owner = owner;
        latitude = latitude; longitude = longitude;
        kind = kind; size = size; difficulty = difficulty; terrain = terrain;
        status = status; hiddenDate = hiddenDate;
        nFounds = nFounds; nNotFounds = nNotFounds; nFavourites = nFavourites;
        altitude = altitude
    }
;;

let rec load_aux ch =
    try (
        let cache = (loadCache ch) in
        cache::(load_aux ch)
    )
    with
    | End_of_file -> []

let load filename = (* post: result is never [] *)
    try (
	    let ch = open_in filename in
	    try (
	   		let final = load_aux ch in
	   		close_in ch;
	   		final
	   	) with e -> (close_in ch; print_string "File not found"; [])
	) with Sys_error -> raise (Arg.bad "File not found")
;;

let count l =
    List.length l
;;

let compare f q1 q2 =
	let res = Pervasives.compare (f q1) (f q2) in
	if res = 0 then Pervasives.compare (q1.code) (q2.code) else res
;;

let hiddenDateSort l = 
	List.rev (List.sort (compare (fun q -> q.hiddenDate)) l) 
;; 	(* //TODO sorting by alphabetical order, fix *)
	(* fix not needed, dates in YYYY/MM/DD, which can be ordered like that *)

let altitudeSort l = 
	let sorted = (List.rev (List.sort (compare (fun q -> q.altitude)) l)) in
	List.filter (fun q -> q.altitude != altitudeUnknown) sorted
;;       (* discard the caches with unknown altitude *)

let nFoundsSort l = 
	List.rev (List.sort (compare (fun q -> q.nFounds)) l) 
;;

let rec comparison c f l =
	match l with
	| [] -> unknownCache
	| x::[] -> x
	| x::xs -> 
		let final = (comparison c f xs) in
		(
		if (f x) = (f final)
			then if x.code > final.code
				then x
				else final
		else if (c (f x) (f final))
			then x
			else final
		)
;;

let smaller f l =
	comparison (fun a b -> a < b) f l
;;

let larger f l =
	comparison (fun a b -> a > b) f l
;;

let northmost l = 
	larger (fun q -> q.latitude) l
;;

let southmost l = 
	smaller (fun q -> q.latitude) l
;;

let eastmost l = 
	larger (fun q -> q.longitude) l
;;

let westmost l =  
	smaller (fun q -> q.longitude) l
;;

let rec countAll f q w =
	match w with
	| [] -> 0
	| x::xs when (f x) = (f q) -> 1 + (countAll f q xs)
	| x::xs -> countAll f q xs
;;

let rec findMatches f w =
	match w with
	| [] -> []
	| x::xs -> (f x, countAll f x w)::(findMatches f xs)
;;

(* //TODO remove duplicates and sorting (done? test) *)

let rec removeAll p w =
	match w with
	| [] -> []
	| x::xs when (fst x) = (fst p) -> removeAll p xs
	| x::xs -> x::(removeAll p xs)
;;

let rec removeDuplicates w =
	match w with
	| [] -> []
	| x::xs -> x::(removeDuplicates (removeAll x xs))
;;

let pairCmp p1 p2 =
	let res = Pervasives.compare (snd p1) (snd p2) in
	if res = 0 
	then (
	if (fst p1) > (fst p2)
	then 1
	else if (fst p1) = (fst p2)
	then 0
	else 0
	)
	(* Pervasives.compare (fst p1) (fst p2) *)
	else res
;;

let anyCount f l =
	let w = findMatches f l in
	let ql = removeDuplicates w in
	List.rev ( List.sort pairCmp ql )
;;

let ownerCount l = 
	let f = (fun x -> x.owner) in
	anyCount f l
;;

let kindCount l = 
	let f = (fun x -> x.kind) in
	anyCount f l
;;

let sizeCount l = 
	let f = (fun x -> x.size) in
	anyCount f l
;;

let stateCount l = 
	let f = (fun x -> x.state) in
	anyCount f l
;;

let terrainCount l = 
	let f = (fun x -> x.terrain) in
	anyCount f l
;;

let difficultyCount l = 
	let f = (fun x -> x.difficulty) in
	anyCount f l
;;

(* tested using regex, have to test more *)

let rec countTerrainDiff d t l =
	match l with
	| [] -> 0
	| x::xs when x.terrain = t && x.difficulty = d ->
		1 + (countTerrainDiff d t xs)
	| x::xs -> countTerrainDiff d t xs
;;

let listDifficulties d w =
	let terrs = [1., 1.5, 2., 2.5, 3., 3.5, 4., 4.5, 5.] in
	List.map (fun x -> countTerrainDiff d x w) terrs
;;

let matrix81 w =
	let diffs = [1., 1.5, 2., 2.5, 3., 3.5, 4., 4.5, 5.] in
	List.map (fun x -> listDifficulties x w) diffs
;;



let matchDate year month date =
	if ((int_of_string (String.sub date 0 4)) = year)
	then if ((int_of_string (String.sub date 5 2)) = month)
	then 1
	else 0
	else 0
;;

let rec countDate year month w =
	match w with
	| [] -> 0
	| x::xs -> (matchDate year month x.hiddenDate)+(countDate year month xs)
;;

let yearList year w =
	let months = [1;2;3;4;5;6;7;8;9;10;11;12] in
	List.map (fun x -> countDate year x w) months
;;

let datesMatrix w =
	let years1 = [2001;2002;2003;2004;2005;2006] in
	let years2 = [2007;2008;2009;2010;2011;2012] in
	List.map (fun x -> yearList x w) (years1@years2)
;;



let rec countNeighbors q l d =
	match l with
	| [] -> 0
	| x::xs when (cacheDistance x q) <= d -> 1 + countNeighbors q xs d
	| x::xs -> countNeighbors q xs d
;;

let rec neighborsCount_aux l d = 
	match l with
	| [] -> []
	| x::xs -> (x.code, (countNeighbors x l d)-1)::(neighborsCount_aux xs d)
;;

let neighborsCount l d =
	let w = neighborsCount_aux l d in
	List.rev (List.sort (fun p1 p2 -> Pervasives.compare (snd p1) (snd p2)) w)
;;
