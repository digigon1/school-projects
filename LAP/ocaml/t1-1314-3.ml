type ctree = Class of string | Selection of string * ctree * ctree;;

let flying = 
	Selection("tem penas?",
		Selection("vive perto da água?",
			Selection("vive perto do mar?",
				Class("gaivota"),
				Class("pato")
			),
			Class("galinha")
		),
		Selection("tem carapaça?",
			Class("besouro"),
			Selection("tem o corpo listrado?",
				Class("abelha"),
				Selection("vive perto da água?",
					Class("libelinha"),
					Class("gafanhoto")
	))));;

let rec belongs ca t =
	match t with
	| Class c -> c=ca
	| Selection(p,y,n) -> belongs ca y || belongs ca n
;;

let rec allClass t =
	match t with
	| Class c -> [c]
	| Selection(p,y,n) -> (allClass y)@(allClass n)
;;

let rec check ca pa t =
	match t with
	| Class c -> false
	| Selection (p,y,n) when p=pa -> belongs ca y
	| Selection (p,y,n) -> (check ca pa y) || (check ca pa n)
;;

let rec allYes pa t =
	match t with
	| Class c -> []
	| Selection (p,y,n) when p=pa -> allClass y
	| Selection (p,y,n) -> (allYes pa y)@(allYes pa n)
;;

let rec allUnknown pa t =
	match t with
	| Class c -> [c]
	| Selection (p,y,n) when p<>pa -> (allUnknown pa y)@(allUnknown pa n)
	| Selection (p,y,n) -> []
;;

let rec minDist pa t =
	match t with
	| Class c -> 1000001
	| Selection (p,y,n) when p=pa -> 0
	| Selection (p,y,n) when minDist pa y<1000001 && minDist pa n<1000001-> (2+abs (minDist pa y-minDist pa n))
	| Selection (p,y,n) -> 1+(min (minDist pa y) (minDist pa n))
;;