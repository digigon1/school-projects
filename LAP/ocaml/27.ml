type exp =
      Add of exp*exp
    | Sub of exp*exp
    | Mult of exp*exp
    | Div of exp*exp
    | Power of exp*int
    | Sim of exp
    | Const of float
    | Var
;;

let rec eval v expr =
	match expr with
	| Var -> v +. 0.0
	| Add (x, y) -> (eval v x) +. (eval v y)
	| Sub (x, y) -> (eval v x) -. (eval v y)
	| Mult (x, y) -> (eval v x) *. (eval v y)
	| Div (x, y) -> (eval v x) /. (eval v y)
	| Power (x, y) -> (eval v x)**(float y)
	| Sim (x) -> 0.0 -. (eval v x)
	| Const (x) -> x
;;

let rec deriv expr =
	match expr with
	| Add (x, y) ->  Add (deriv x, deriv y)
	| Sub (x, y) -> Sub (deriv x, deriv y)
	| Mult (x, y) -> Add (Mult (deriv x, y), Mult (x, deriv y))
	| Div (x, y) -> Div (Sub (Mult (deriv x, y), Mult (x, deriv y)), Power (y, 2))
	| Power (x, y) -> Mult (Const(float y), Power (x, y-1))
	| Sim (x) -> Sim (deriv x)
	| Const (x) -> Const (0.0)
	| Var -> Const (1.0)
;;