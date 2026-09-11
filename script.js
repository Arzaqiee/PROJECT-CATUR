const U={wK:"♔",wQ:"♕",wR:"♖",wB:"♗",wN:"♘",wP:"♙",bK:"♚",bQ:"♛",bR:"♜",bB:"♝",bN:"♞",bP:"♟"};
let board,turn="w",selected=null,stack=[],moves=[],gameOver=false,mode="bot",thinking=false;
let baseTime=600,whiteTime=600,blackTime=600,timer=null,lastMove=null;
let settings=JSON.parse(localStorage.getItem("qieSettings")||'{"sound":true,"vibe":true,"coord":true,"legal":true,"theme":"classic"}');

function startBoard(){return[
["bR","bN","bB","bQ","bK","bB","bN","bR"],["bP","bP","bP","bP","bP","bP","bP","bP"],
[null,null,null,null,null,null,null,null],[null,null,null,null,null,null,null,null],
[null,null,null,null,null,null,null,null],["wP","wP","wP","wP","wP","wP","wP","wP"],
["wR","wN","wB","wQ","wK","wB","wN","wR"]];
}
function render(){const el=document.querySelector("#board");el.innerHTML="";
for(let r=0;r<8;r++)for(let c=0;c<8;c++){let s=document.createElement("div");s.className="square "+((r+c)%2?"dark":"light");s.onclick=()=>tap(r,c);
let p=board[r][c];if(p){let x=document.createElement("div");x.className="piece "+(p[0]=="w"?"white-piece":"black-piece");x.textContent=U[p];s.appendChild(x)}
if(settings.coord){if(c===0){let a=document.createElement("span");a.className="coord rank";a.textContent=8-r;s.appendChild(a)}if(r===7){let a=document.createElement("span");a.className="coord file";a.textContent="abcdefgh"[c];s.appendChild(a)}}
if(selected?.r===r&&selected?.c===c)s.classList.add("selected");if(lastMove?.some(x=>x.r===r&&x.c===c))s.classList.add("last");
el.appendChild(s)}
if(selected&&settings.legal) legalHighlight();
}
function inside(r,c){return r>=0&&r<8&&c>=0&&c<8}
function movesFor(r,c){let p=board[r][c],out=[];if(!p)return out;let col=p[0],t=p[1],add=(a,b)=>{if(inside(a,b)&&(!board[a][b]||board[a][b][0]!=col))out.push({r:a,c:b})};
if(t=="P"){let d=col=="w"?-1:1,start=col=="w"?6:1;if(inside(r+d,c)&&!board[r+d][c]){out.push({r:r+d,c});if(r==start&&!board[r+2*d][c])out.push({r:r+2*d,c})}for(let dc of[-1,1]){let nr=r+d,nc=c+dc;if(inside(nr,nc)&&board[nr][nc]&&board[nr][nc][0]!=col)out.push({r:nr,c:nc})}}
if(t=="N")for(let [a,b] of[[-2,-1],[-2,1],[-1,-2],[-1,2],[1,-2],[1,2],[2,-1],[2,1]])add(r+a,c+b);
if(t=="K")for(let a=-1;a<=1;a++)for(let b=-1;b<=1;b++)if(a||b)add(r+a,c+b);
let slide=(dirs)=>dirs.forEach(([dr,dc])=>{let a=r+dr,b=c+dc;while(inside(a,b)){if(!board[a][b])out.push({r:a,c:b});else{if(board[a][b][0]!=col)out.push({r:a,c:b});break}a+=dr;b+=dc}});
if(t=="B"||t=="Q")slide([[-1,-1],[-1,1],[1,-1],[1,1]]);if(t=="R"||t=="Q")slide([[-1,0],[1,0],[0,-1],[0,1]]);
return out}
function legalHighlight(){let ss=document.querySelectorAll(".square");movesFor(selected.r,selected.c).forEach(m=>ss[m.r*8+m.c].classList.add(board[m.r][m.c]?"capture":"legal"))}
function tap(r,c){if(gameOver||thinking||(mode=="bot"&&turn=="b"))return;let p=board[r][c];
if(selected){let ok=movesFor(selected.r,selected.c).some(m=>m.r==r&&m.c==c);if(ok){doMove(selected.r,selected.c,r,c);selected=null;render();if(!gameOver&&mode=="bot"&&turn=="b")bot();return}if(p&&p[0]==turn){selected={r,c};render();return}selected=null;render();return}
if(p&&p[0]==turn){selected={r,c};render()}}
function doMove(fr,fc,tr,tc){stack.push(JSON.stringify(board));let p=board[fr][fc],cap=board[tr][tc];board[tr][tc]=p;board[fr][fc]=null;
if(p[1]=="P"&&((p[0]=="w"&&tr==0)||(p[0]=="b"&&tr==7)))board[tr][tc]=p[0]+"Q";
lastMove=[{r:fr,c:fc},{r:tr,c:tc}];moves.push(notation(p,fr,fc,tr,tc,cap));turn=turn=="w"?"b":"w";sound(cap?"capture":"move");vibe();renderMoves();updateStatus();checkEnd()}
function notation(p,fr,fc,tr,tc,cap){let z={K:"K",Q:"Q",R:"R",B:"B",N:"N",P:""}[p[1]];return z+(cap?"x":"")+"abcdefgh"[tc]+(8-tr)}
function bot(){thinking=true;updateStatus();setTimeout(()=>{let all=[];for(let r=0;r<8;r++)for(let c=0;c<8;c++)if(board[r][c]?.[0]=="b")movesFor(r,c).forEach(m=>all.push({fr:r,fc:c,...m}));
if(!all.length){end("BOT tidak punya langkah.");return}let caps=all.filter(x=>board[x.r][x.c]?.[0]=="w"),a=(caps.length?caps:all)[Math.floor(Math.random()*(caps.length?caps.length:all.length))];doMove(a.fr,a.fc,a.r,a.c);thinking=false;render()},430)}
function checkEnd(){let any=false;for(let r=0;r<8;r++)for(let c=0;c<8;c++)if(board[r][c]?.[0]==turn&&movesFor(r,c).length){any=true;break}if(!any)end(turn=="w"?"BOT menang.":"Kamu menang.")}
function end(msg){gameOver=true;thinking=false;clearInterval(timer);document.querySelector("#status").textContent=msg;sound("end");saveHistory(msg);toast(msg)}
function undo(){if(!stack.length||gameOver)return;board=JSON.parse(stack.pop());moves.pop();turn="w";lastMove=null;selected=null;thinking=false;render();renderMoves();updateStatus()}
function restart(){board=startBoard();turn="w";selected=null;stack=[];moves=[];gameOver=false;thinking=false;lastMove=null;whiteTime=blackTime=baseTime;render();renderMoves();updateStatus();startTimer()}
function renderMoves(){document.querySelector("#moves").innerHTML=moves.length?moves.map((x,i)=>i%2==0?`<span><b>${Math.floor(i/2)+1}.</b> ${x} `:x+"</span> ").join(""):"No moves yet."}
function updateStatus(){if(gameOver)return;document.querySelector("#status").textContent=thinking?"BOT is thinking…":turn=="w"?"Your turn":"Black's turn";document.querySelector("#whiteClock").classList.toggle("active",turn=="w");document.querySelector("#blackClock").classList.toggle("active",turn=="b")}
function fmt(s){return String(Math.floor(s/60)).padStart(2,"0")+":"+String(s%60).padStart(2,"0")}
function startTimer(){clearInterval(timer);timer=setInterval(()=>{if(gameOver)return;if(turn=="w")whiteTime--;else blackTime--;document.querySelector("#whiteClock").textContent=fmt(Math.max(0,whiteTime));document.querySelector("#blackClock").textContent=fmt(Math.max(0,blackTime));if(whiteTime<=0)end("Waktu habis. BOT menang.");if(blackTime<=0)end("Waktu BOT habis. Kamu menang.")},1000)}
function sound(type){if(!settings.sound)return;try{let C=window.AudioContext||window.webkitAudioContext,c=new C(),o=c.createOscillator(),g=c.createGain();let f={click:520,move:390,capture:210,end:150}[type]||390;o.frequency.value=f;o.type="sine";g.gain.setValueAtTime(.0001,c.currentTime);g.gain.exponentialRampToValueAtTime(.08,c.currentTime+.01);g.gain.exponentialRampToValueAtTime(.0001,c.currentTime+.09);o.connect(g);g.connect(c.destination);o.start();o.stop(c.currentTime+.1)}catch(e){}}
function vibe(){if(settings.vibe&&navigator.vibrate)navigator.vibrate(18)}
function toast(t){let e=document.querySelector("#toast");e.textContent=t;e.classList.add("show");setTimeout(()=>e.classList.remove("show"),1500)}
function show(id){document.querySelectorAll(".screen").forEach(x=>x.classList.remove("active"));document.querySelector("#"+id).classList.add("active")}
function saveHistory(result){let h=JSON.parse(localStorage.getItem("qieHistory")||"[]");h.unshift({date:new Date().toLocaleString("id-ID"),mode:mode,result,moves:moves.length});localStorage.setItem("qieHistory",JSON.stringify(h.slice(0,30)))}
function loadHistory(){let h=JSON.parse(localStorage.getItem("qieHistory")||"[]");document.querySelector("#historyList").innerHTML=h.length?h.map(x=>`<div style="padding:9px 0;border-bottom:1px solid #28282d"><b>${x.result}</b><br><small>${x.date} · ${x.mode} · ${x.moves} moves</small></div>`).join(""):"No completed games yet."}
function applySettings(){localStorage.setItem("qieSettings",JSON.stringify(settings));document.body.classList.toggle("mono",settings.theme=="mono");document.querySelector("#soundToggle").checked=settings.sound;document.querySelector("#vibeToggle").checked=settings.vibe;document.querySelector("#coordToggle").checked=settings.coord;document.querySelector("#legalToggle").checked=settings.legal;document.querySelector("#theme").value=settings.theme;render()}
document.querySelectorAll("[data-mode]").forEach(b=>b.onclick=()=>{mode=b.dataset.mode;document.querySelector("#modeTitle").textContent=mode=="bot"?"VS BOT":mode=="local"?"LOCAL 2 PLAYER":"VS FRIEND";document.querySelector("#blackName").textContent=mode=="bot"?"BOT":"PLAYER 2";show("game");restart()});
document.querySelectorAll("[data-open]").forEach(b=>b.onclick=()=>{show(b.dataset.open);if(b.dataset.open=="history")loadHistory()});
document.querySelectorAll("[data-back]").forEach(b=>b.onclick=()=>show("menu"));
document.querySelector("#backMenu").onclick=()=>{clearInterval(timer);show("menu")};
document.querySelector("#gameMenu").onclick=()=>show("settings");
document.querySelector("#restart").onclick=restart;document.querySelector("#undo").onclick=undo;
document.querySelector("#resign").onclick=()=>{if(!gameOver)end("Kamu menyerah. BOT menang.")};
document.querySelector("#draw").onclick=()=>toast("Permintaan remis dikirim.");
document.querySelector("#clearHistory").onclick=()=>{localStorage.removeItem("qieHistory");loadHistory()};
document.querySelector("#soundToggle").onchange=e=>{settings.sound=e.target.checked;applySettings()};document.querySelector("#vibeToggle").onchange=e=>{settings.vibe=e.target.checked;applySettings()};document.querySelector("#coordToggle").onchange=e=>{settings.coord=e.target.checked;applySettings()};document.querySelector("#legalToggle").onchange=e=>{settings.legal=e.target.checked;applySettings()};document.querySelector("#theme").onchange=e=>{settings.theme=e.target.value;applySettings()};
board=startBoard();applySettings();
