(* Geocaching module interface *)
(* LAP (AMD 2016) *)

(*
01234567890123456789012345678901234567890123456789012345678901234567890123456789
   80 columns
*)

type cache = {
    code: string; name: string; state: string; owner: string;
    latitude: float; longitude: float;
    kind: string; size: string; difficulty: float; terrain: float;
    status: string; hiddenDate: string;
    nFounds: int; nNotFounds: int; nFavourites: int;
    altitude: int
} ;;

val load: string -> cache list
val count: cache list -> int

val hiddenDateSort: cache list -> cache list
val altitudeSort: cache list -> cache list
val nFoundsSort: cache list -> cache list

val northmost: cache list -> cache
val southmost: cache list -> cache
val eastmost: cache list -> cache
val westmost: cache list -> cache

val ownerCount: cache list -> (string * int) list
val kindCount: cache list -> (string * int) list
val sizeCount: cache list -> (string * int) list
val stateCount: cache list -> (string * int) list
val terrainCount: cache list -> (float * int) list
val difficultyCount: cache list -> (float * int) list

val matrix81: cache list -> int list list
val datesMatrix: cache list -> int list list

val neighborsCount: cache list -> float -> (string * int) list







