/*	 O Regresso do Barão!!!

Goncalo Almeida (45353)
Jose Martins (45678)

01234567890123456789012345678901234567890123456789012345678901234567890123456789
*/

// GLOBAL VARIABLES

// tente não definir mais nenhuma variável global

var ctx, empty, baron, world, control;


// ACTORS

var Actor = EXTENDS(JSRoot, {
	x: 0, y: 0,
	image: null,
	time: 0,
	dying: false,
	count: 0,
	msg: "",
	INIT: function(x, y, kind) {
		this.x = x;
		this.y = y;
		this.image = GameImage.get(kind, "").image;
		this.time = 0;
		this.dying = false;
		this.show();
	},
	show: function() {
		world[this.x][this.y] = this;
		ctx.drawImage(this.image, this.x * ACTOR_PIXELS_X, this.y * ACTOR_PIXELS_Y);
	},
	hide: function() {
		world[this.x][this.y] = empty;
		ctx.drawImage(empty.image, this.x * ACTOR_PIXELS_X, this.y * ACTOR_PIXELS_Y);
	},
	move: function(dx, dy) {
		this.hide();
		var neighbour = world[this.x + dx][this.y + dy];
		if(neighbour == empty){
			this.x += dx;
			this.y += dy;
		} else {
			this.show();
			return false;
		}
		this.show();
		return true;
	},
	animation: function() {
		if(this.dying){
			if(this.count%2==0)
				ctx.drawImage(empty.image, this.x * ACTOR_PIXELS_X, this.y * ACTOR_PIXELS_Y);
			else
				ctx.drawImage(this.image, this.x * ACTOR_PIXELS_X, this.y * ACTOR_PIXELS_Y);
		
			this.count++;
			if(this.count >= 10){
				this.die(this.msg);
			}
		}
	},
	die: function (msg) {	
	},
	flashDie: function(msg){
		this.msg = msg;
		this.dying = true;
	},
	movable: function(){
		return true;
	},
	grab: function() {},
	nextLevel: function() {},
	stop: function() {},
	hit: function() {},
	smash: function() {},
	attack: function() {},
	ballHit: function() {},
});

var Inert = EXTENDS(Actor, {
	INIT: function(x, y, type){
		this.SUPER(Actor.INIT, x, y, type);
	},
	move: function(dx, dy){
		return false;
	},
	movable: function(){
		return false;
	},
	flashDie: function(msg){},
});

var Empty = EXTENDS(Actor, {
	INIT: function() {
		this.SUPER(Actor.INIT, -1, -1, "Empty");
	},
	show: function() {},
	hide: function() {},
	move: function() {return false;},
});

var Block = EXTENDS(Inert, {
	INIT: function(x, y) {
		this.SUPER(Inert.INIT, x, y, "Block");
	},
	
})

var Endable = EXTENDS(JSRoot, {
	nextLevel: function(){
		mesg("Congratulations!");
		control.nextLevel();
	}
})

var Sun = EXTENDS(Inert, EXTENDS(Endable, {
		INIT: function(x, y) {
			this.SUPER(Inert.INIT, x, y, "Sun");
		},
		animation: function(){
			var neighbour = world[this.x][this.y-1];
			neighbour.nextLevel();
		},
	}))


var Movable = EXTENDS(Actor, {
	dx: 0, dy: 0,
	speed: 0,
	mov: 0,
	INIT: function(x, y, type, dx, dy, speed){
		this.SUPER(Actor.INIT, x, y, type);
		this.dx = dx;
		this.dy = dy;
		this.speed = speed;
		this.mov = 0;
	},
	animation : function(){
		if(this.dying){
			this.SUPER(Actor.animation);
			return false;
		} else {
			var neighbour = world[this.x + this.dx][this.y + this.dy];
			if(neighbour == empty && this.mov >= ANIMATION_EVENTS_PER_SECOND/this.speed){
				this.move(this.dx, this.dy);
				this.mov = 0;
			} else if(!neighbour.movable() && this.mov >= ANIMATION_EVENTS_PER_SECOND/this.speed){
				this.mov = 1;
				return false;
			} else if(this.mov >= ANIMATION_EVENTS_PER_SECOND/this.speed){
				this.mov = 0;
			}
			this.mov++;
			return true;
		}
	},
	move: function(dx,dy){
		if(!this.SUPER(Actor.move, dx, dy)){
			this.flashDie();
		}
	},
	die: function(){
		this.hide();
	}
});

var Jerrycan = EXTENDS(Movable, {
	INIT: function(x, y) {
		this.SUPER(Movable.INIT, x, y, "Jerrycan", 0, 1, JERRYCAN_SPEED);
	},
	animation : function(){
		var neighbour = world[this.x + this.dx][this.y + this.dy];
		if(!Movable.animation.call(this)){
			neighbour.smash("Killed by a Jerrycan");
		}
		world[this.x][this.y+1].nextLevel();
	},
	move: function(dx, dy){
		this.SUPER(Actor.move, dx, dy);
	},
	die: function() {},
})

var Weight = EXTENDS(Movable, {
	INIT: function(x, y) {
		this.SUPER(Movable.INIT, x, y, "Weight", 0, 1, WEIGHT_SPEED);
	},
	animation : function(){
		var neighbour = world[this.x + this.dx][this.y + this.dy];
		if(!Movable.animation.call(this)){
			neighbour.smash("Killed by a Falling Weight.");
		}
	},

})

var Ballon = EXTENDS(Movable, {
	INIT: function(x, y) {
		this.SUPER(Movable.INIT, x, y, "Ballon", 0, -1, BALLOON_SPEED);
	},
	animation : function(){
		var neighbour = world[this.x + this.dx][this.y + this.dy];
		if(!Movable.animation.call(this)){
			neighbour.hit();
		}
	},
})

var Weapon = EXTENDS(Actor , {
	dx: 0, dy: 0,
	moving: false,
	INIT: function (x, y, type, moving, dx, dy) {
		this.SUPER(Actor.INIT, x, y, type);
		this.moving = moving;
		this.dx = dx;
		this.dy = dy;
	},
	die: function (msg) {},
	stop: function(){
		this.moving = false;
	},
})

var Ball = EXTENDS(Weapon, {
	moving: false,
	dx: 0, dy: 0,
	INIT: function(x, y, moving, dx, dy) {
		this.SUPER(Weapon.INIT, x, y, "Ball", moving, dx, dy);
	},
	grab: function(){
		baron.balls.push(1);
		this.hide();
		var balls = baron.balls.length;
		document.getElementById("balls").innerHTML = balls+" bola"+((balls!=1)?"s":"")+" restante"+((balls!=1)?"s":"");
	},
	animation: function(){
		if(this.moving){
			if(!this.move(this.dx, this.dy)){
				var neighbour = world[this.x + this.dx][this.y + this.dy];
				neighbour.ballHit("Hit by ball");
				this.stop();
			}
		}
	},
})

var Enemy = EXTENDS(Actor, {
	mov: 0,
	speed: 0,
	INIT: function(x, y, type, speed){
		this.SUPER(Actor.INIT, x, y, type);
		this.mov = 0;
		this.speed = speed;
	},
	animation: function(){
		this.SUPER(Actor.animation);
	},
	die: function(){ this.hide(); },
	smash: function() { this.flashDie(); },
	ballHit: function() { this.flashDie(); },
});

var Mammoth = EXTENDS(Enemy, {
	INIT: function(x, y) {
		this.SUPER(Enemy.INIT, x, y, "Mammoth", MAMMOTH_SPEED);
	},
	movable: function() {
		return false;
	},
	hit: function(msg){
		this.flashDie(msg);
	},
	animation: function(){
		if(this.dying){
			this.SUPER(Enemy.animation);
		} else {
			if(this.mov >= ANIMATION_EVENTS_PER_SECOND/this.speed){
				var dx = 0;
				var dy = 0;

				var leftDist = this.x - baron.x;
       			var upDist = this.y - baron.y;
       			if (upDist > 0) {
            		dy = -1;
        		} else if (upDist < 0) {
            		dy = 1;
        		} 
        		if (leftDist > 0) {
            		dx = -1;
        		} else if(leftDist < 0){
            		dx = 1;
        		}
        		if(!this.move(dx, dy)){
        			var neighbour = world[this.x + dx][this.y + dy];
        			neighbour.attack("Killed by Mammoth");
        		}
        		this.mov = 0;
        	}
        	this.mov++;
		}
	}
})

var Baron = EXTENDS(Actor, {
	balls: [],
	dx: 0, dy: 0,
	lives: BARON_LIVES,
	INIT: function(x, y) {
		this.SUPER(Actor.INIT, x, y, "Baron");
		this.balls = [];
		this.dx = 0;
		this.dy = 0;
		this.lives = BARON_LIVES;
	},
	animation: function() {
		if(this.dying){
			this.SUPER(Actor.animation);
		} else {
			var d = control.getKey();
			if( d == null ) return;
			if( d == 1 ) {
				this.shoot();
				return;
			}
			this.dx = d[0]; this.dy = d[1];
			var neighbour = world[this.x + this.dx][this.y + this.dy];
			if(this.balls.length < 10)
				neighbour.grab();

			this.move(this.dx, this.dy);
		}
	},
	die: function(msg){
		if(msg != undefined)
			mesg(msg);
		if(this.lives > 0){
			var lives_rem = this.lives-1;
			mesg(lives_rem+" lives left");
			control.loadLevel(control.level);
			baron.lives = lives_rem;
		} else {
			control.gameOver();
		}
	},
	shoot: function(){
		if(this.balls.length != 0){
			if(world[this.x + this.dx][this.y + this.dy] == empty){
				this.balls.pop();
				NEW(Ball, this.x + this.dx, this.y + this.dy, true, this.dx, this.dy);
				var balls = this.balls.length;
				document.getElementById("balls").innerHTML = balls+" bola"+((balls!=1)?"s":"")+" restante"+((balls!=1)?"s":"");
			}
		}
	},
	move: function(dx, dy){
		var neighbour = world[this.x + this.dx][this.y + this.dy];
		if(neighbour.movable()){
			neighbour.move(dx, dy);
		}
		Actor.move.call(this, dx, dy);
	},
	smash: function(msg){
		this.flashDie(msg);
	},
	movable: function(){
		return false;
	},
	attack: function(msg){
		this.flashDie(msg);
	},
})


// GAME CONTROL

var GameControl = EXTENDS(JSRoot, {
	key: 0,
	time: 0,
	level: 1,
	score: 0,
	INIT: function() {
		ctx = document.getElementById("canvas1").getContext("2d");
		empty = NEW(Empty);	// only one empty actor needed
		world = this.createWorld();
		this.loadLevel(this.level);
		this.setupEvents();
		control = this;
	},
	createWorld: function () { // stored by columns
		var matrix = new Array(WORLD_WIDTH);
		for( var x = 0 ; x < WORLD_WIDTH ; x++ ) {
			var a = new Array(WORLD_HEIGHT);
			for( var y = 0 ; y < WORLD_HEIGHT ; y++ )
				a[y] = empty;
			matrix[x] = a;
		}
		return matrix;
	},
	loadLevel: function(level) {
		document.getElementById("balls").innerHTML = "0 bolas restantes"
		this.time = 0;
		document.getElementById("nivel").innerHTML = "Nivel "+level;
		if( level < 1 || level > MAPS.length )
			fatalError("Invalid level " + level)
		var map = MAPS[level-1];  // -1 because levels start at 1
		for(var x=0 ; x < WORLD_WIDTH ; x++)
			for(var y=0 ; y < WORLD_HEIGHT ; y++) {
				world[x][y].hide();
				var code = map[y][x];  // x/y reversed because map stored by lines
				var gi = GameImage.getByCode(code);
				if( gi ) {
					var a = NEW(globalByName(gi.kind), x, y);
					if( gi.kind == "Baron" )
						baron = a;
				}
			}
	},
	getKey() {
		var k = this.key;
		this.key = 0;
		switch( k ) {
			case 37: case 79: case 74: return [-1, 0]; //  LEFT, O, J
			case 38: case 81: case 73: return [0, -1]; //    UP, Q, I
			case 39: case 80: case 76: return [1, 0];  // RIGHT, P, L
			case 40: case 65: case 75: return [0, 1];  //  DOWN, A, K
			case 90: return 1; //	SHOOT (Z)
			case 0: return null;
			default: return String.fromCharCode(k);
		// http://www.cambiaresearch.com/articles/15/javascript-char-codes-key-codes
		};	
	},
	setupEvents: function() {
		addEventListener("keydown", this.keyDownEvent, false);
		addEventListener("keyup", this.keyUpEvent, false);
		setInterval(this.animationEvent, 1000 / ANIMATION_EVENTS_PER_SECOND);
	},
	animationEvent: function() {
		control.time++;
		if(control.time > 1200){
			baron.die("Time's up!");
		}
		document.getElementById("time").innerHTML = (control.time/ANIMATION_EVENTS_PER_SECOND).toFixed(0) + " segundos";
		for(var x=0 ; x < WORLD_WIDTH ; x++)
			for(var y=0 ; y < WORLD_HEIGHT ; y++) {
				var a = world[x][y];
				if( a.time < control.time ) {
					a.time = control.time;
					a.animation();
				}
			}
	},
	keyDownEvent: function(k) { control.key = k.keyCode; },
	keyUpEvent: function(k) { },
	gameOver: function() {
		mesg("Game Over!");
		mesg("Score: "+this.score);
		mesg("Restarting game!");
		this.time = 0;
		this.level = 1
		this.loadLevel(this.level);
	},
	nextLevel: function() {
		this.level++;
		if(this.level > MAPS.length){
			mesg("You beat the game!");
			this.gameOver();
		}
		var l_score = ((1200-this.time)/ANIMATION_EVENTS_PER_SECOND).toFixed(0);
		l_score = Number(l_score);
		l_score += 200*baron.lives;
		this.score += l_score;
		document.getElementById("score").innerHTML = this.score+" pontos"
		this.loadLevel(this.level);
	}
});


// HTML FORM

function onLoad() {
  // load images an then run the game
	GameImage.loadImages(function() {NEW(GameControl);});

}

function b1() { mesg("button1") }
function b2() { mesg("button2") }



