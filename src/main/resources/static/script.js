// Gets a handle to the element with id Chess.
const canvas = document.getElementById("Chess");
// Set the canvas up for drawing in 2D.
const ctx = canvas.getContext("2d");

// sample hard coded response from the server. Contains the current state of the board.
const board = {
    "positions": {
        "A1": "wRook",
        "A2": "wPawn",
        "A7": "bPawn",
        "A8": "bRook",
        "B1": "wKnight",
        "B2": "wPawn",
        "B7": "bKnight",
        "B8": "bPawn",
        "C1": "wBishop",
        "C2": "wPawn",
        "C7": "bPawn",
        "C8": "bBishop",
        "D1": "wKing",
        "D2": "wPawn",
        "D7": "bPawn",
        "D8": "bKing",
        "E1": "wQueen",
        "E2": "wPawn",
        "E7": "bPawn",
        "E8": "bQueen",
        "F1": "wBishop",
        "F2": "wPawn",
        "F7": "bBishop",
        "F8": "bPawn",
        "G1": "wKnight",
        "G2": "wPawn",
        "G7": "bKnight",
        "G8": "bPawn",
        "H1": "wRook",
        "H2": "wPawn",
        "H7": "bPawn",
        "H8": "bRook"
    }
};

// Map an x/y co-ordinate to the chess location.
function mapToChess(x, y) {
    const a_ascii_code = 65;
    const mapped_int = BOARD_SIZE - y;
    return String.fromCharCode(a_ascii_code + x) + "" + mapped_int;
}

const GRID_SIZE = 63;
const BOARD_SIZE = 8;

function getMousePos(canvas, evt) {
    const rect = canvas.getBoundingClientRect();
    return {
        x: evt.clientX - rect.left,
        y: evt.clientY - rect.top
    };
}

canvas.addEventListener("click", function (e) {
    const pos = getMousePos(canvas, e);
    const boardPos = {
        x: Math.floor(pos.x / GRID_SIZE),
        y: Math.floor(pos.y / GRID_SIZE)
    };
    alert(mapToChess(boardPos.x, boardPos.y));
});

function isBlackSquare(x, y) {
    return x % 2 === y % 2;
}

function draw() {
    for (let x = 0; x < BOARD_SIZE; x++) {
        for (let y = 0; y < BOARD_SIZE; y++) {
            if (isBlackSquare(x, y)) {
                drawSquare("black", x, y);
            }
            else {
                drawSquare("white", x, y)
            }
        }
    }
}

// fills up the square of the chess board with either black or white.
function drawSquare(colour, x, y) {
    const image = new Image();
    const currentChessPosition = mapToChess(x, y); // ex. 0,0 -> H1
    // the name of the image from the response matches the name of the image files.
    image.src = "images/" + board.positions[currentChessPosition] + ".png";
    image.onload = () => {
        ctx.fillStyle = colour;
        // draw a rectangle
        ctx.fillRect(GRID_SIZE * x, GRID_SIZE * y, GRID_SIZE, GRID_SIZE);
        // and then an image on top of it.
        ctx.drawImage(image, GRID_SIZE * x, GRID_SIZE * y, GRID_SIZE, GRID_SIZE);
    };
}

function start() {
    draw();
    window.requestAnimationFrame(start);
}

start();