// ─────────────────────────────────────────────
//  TIC TAC TOE — Java Backend Only
//  All game logic runs in Spring Boot /api/game/*
// ─────────────────────────────────────────────

// ── State ─────────────────────────────────────
let sessionId      = null;
let currentMode    = null;
let pendingMode    = null;   // mode chosen before name entry
let playerNames    = { X: 'Player 1', O: 'Player 2' };
let scores         = { X: 0, O: 0, draws: 0 };
let boardLocked    = false;

// ── DOM refs ──────────────────────────────────
const menuScreen    = document.getElementById('menuScreen');
const gameScreen    = document.getElementById('gameScreen');
const modeGrid      = document.getElementById('modeGrid');
const namePanel     = document.getElementById('namePanel');
const namePanelHdr  = document.getElementById('namePanelHeader');
const nameFields    = document.getElementById('nameFields');
const modeBadge     = document.getElementById('modeBadge');
const board         = document.getElementById('board');
const cells         = [...document.querySelectorAll('.cell')];
const turnIndicator = document.getElementById('turnIndicator');
const scoreXNum     = document.getElementById('scoreXNum');
const scoreONum     = document.getElementById('scoreONum');
const drawCount     = document.getElementById('drawCount');
const labelX        = document.getElementById('labelX');
const labelO        = document.getElementById('labelO');
const resultOverlay = document.getElementById('resultOverlay');
const resultMark    = document.getElementById('resultMark');
const resultMsg     = document.getElementById('resultMsg');
const serverStatus  = document.getElementById('serverStatus');
const cursorDot     = document.getElementById('cursorDot');
const cancelModeBtn = document.getElementById('cancelModeBtn');
const launchBtn     = document.getElementById('launchBtn');

// ── Mode metadata ─────────────────────────────
// HC: Human (X) vs Computer (O)
// HH: Human (X) vs Human (O)
// CC: Computer vs Computer
const MODE_META = {
  HH: { badge: 'H vs H',    hasX: true,  hasO: true  },
  HC: { badge: 'H vs CPU',  hasX: true,  hasO: false },
  CC: { badge: 'CPU vs CPU',hasX: false, hasO: false },
};

// ── Custom cursor ─────────────────────────────
document.addEventListener('mousemove', e => {
  cursorDot.style.left = e.clientX + 'px';
  cursorDot.style.top  = e.clientY + 'px';
});
cells.forEach(c => {
  c.addEventListener('mouseenter', () => cursorDot.classList.add('on-cell'));
  c.addEventListener('mouseleave', () => cursorDot.classList.remove('on-cell'));
});

// ── API helpers ───────────────────────────────
async function apiGet(path) {
  const res = await fetch(path);
  if (!res.ok) throw new Error(`GET ${path} → ${res.status}`);
  return res.json();
}

async function apiPost(path, body) {
  const res = await fetch(path, {
    method:  'POST',
    headers: { 'Content-Type': 'application/json' },
    body:    JSON.stringify(body)
  });
  if (!res.ok) {
    const txt = await res.text().catch(() => '');
    throw new Error(`POST ${path} → ${res.status} ${txt}`);
  }
  return res.json();
}

// ── Server health check ───────────────────────
async function checkServer() {
  serverStatus.textContent = 'CHECKING SERVER…';
  serverStatus.className   = 'server-status checking';
  try {
    const data = await apiGet('/api/game/health');
    serverStatus.textContent = data.status || '✅ SERVER ONLINE';
    serverStatus.className   = 'server-status ok';
  } catch {
    serverStatus.textContent = '❌ SERVER OFFLINE — start Spring Boot';
    serverStatus.className   = 'server-status fail';
  }
}

// ── Mode button click → show name panel ───────
modeGrid.querySelectorAll('.mode-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    const mode = btn.dataset.mode;

    // Highlight selected button
    modeGrid.querySelectorAll('.mode-btn').forEach(b => b.classList.remove('selected'));
    btn.classList.add('selected');

    pendingMode = mode;
    showNamePanel(mode);
  });
});

function showNamePanel(mode) {
  const meta = MODE_META[mode];
  namePanel.classList.remove('hidden');

  // Build header text
  namePanelHdr.textContent =
    mode === 'CC' ? 'COMPUTER VS COMPUTER' : 'ENTER PLAYER NAME(S)';

  // Build input fields
  nameFields.innerHTML = '';

  if (meta.hasX) {
    nameFields.appendChild(makeInputField('p1Name', 'X', 'PLAYER 1 (X)', 'e.g. Alice'));
  } else {
    nameFields.appendChild(makeCpuTag('CPU — PLAYER X', '🤖'));
  }

  if (meta.hasO) {
    nameFields.appendChild(makeInputField('p2Name', 'O', 'PLAYER 2 (O)', 'e.g. Bob'));
  } else {
    nameFields.appendChild(makeCpuTag('CPU — PLAYER O', '🤖'));
  }

  // Auto-focus first input if any
  const firstInput = nameFields.querySelector('input');
  if (firstInput) setTimeout(() => firstInput.focus(), 50);
}

function makeInputField(id, mark, labelText, placeholder) {
  const wrapper = document.createElement('div');
  wrapper.className = 'name-field';

  const label = document.createElement('label');
  label.htmlFor = id;
  const markSpan = document.createElement('span');
  markSpan.className = mark === 'X' ? 'mark-x' : 'mark-o';
  markSpan.textContent = `[${mark}]`;
  label.appendChild(markSpan);
  label.appendChild(document.createTextNode(' ' + labelText));

  const input = document.createElement('input');
  input.type        = 'text';
  input.id          = id;
  input.className   = 'name-input';
  input.placeholder = placeholder;
  input.maxLength   = 18;
  // Press Enter to launch
  input.addEventListener('keydown', e => { if (e.key === 'Enter') launchBtn.click(); });

  wrapper.appendChild(label);
  wrapper.appendChild(input);
  return wrapper;
}

function makeCpuTag(text, icon) {
  const div = document.createElement('div');
  div.className = 'cpu-tag';
  div.innerHTML = `<span class="robot">${icon}</span> ${text}`;
  return div;
}

// ── Cancel / back from name panel ─────────────
cancelModeBtn.addEventListener('click', () => {
  namePanel.classList.add('hidden');
  modeGrid.querySelectorAll('.mode-btn').forEach(b => b.classList.remove('selected'));
  pendingMode = null;
});

// ── Launch button → start game ─────────────────
launchBtn.addEventListener('click', async () => {
  if (!pendingMode) return;

  const meta = MODE_META[pendingMode];

  // Read names (or default)
  const p1Input = document.getElementById('p1Name');
  const p2Input = document.getElementById('p2Name');

  const p1Name = (p1Input?.value.trim()) || 'Player 1';
  const p2Name = (p2Input?.value.trim()) || 'Player 2';

  playerNames = {
    X: meta.hasX ? p1Name : 'CPU',
    O: meta.hasO ? p2Name : 'CPU',
  };

  await startGame(pendingMode, p1Name, p2Name);
});

// ── Start game ────────────────────────────────
async function startGame(mode, p1Name, p2Name) {
  currentMode = mode;
  boardLocked = false;

  const meta  = MODE_META[mode];
  modeBadge.textContent = meta.badge;
  labelX.textContent    = playerNames.X.toUpperCase();
  labelO.textContent    = playerNames.O.toUpperCase();

  menuScreen.classList.add('hidden');
  gameScreen.classList.remove('hidden');
  resultOverlay.classList.add('hidden');

  // Reset board visually
  cells.forEach(c => { c.className = 'cell'; c.textContent = ''; });

  try {
    const data = await apiPost('/api/game/start', { mode, p1Name, p2Name });
    sessionId = data.sessionId;
    renderBoard(data.board);
    setTurn(data.currentPlayer);

    // If CC mode, CPU plays first immediately
    if (mode === 'CC' && data.status === 'ongoing') {
      await triggerCpuMove();
    }
  } catch (err) {
    alert('Could not start game.\n' + err.message +
          '\n\nMake sure Spring Boot is running at http://localhost:8080');
  }
}

// ── Cell click → human move ───────────────────
cells.forEach(cell => {
  cell.addEventListener('click', () => handleCellClick(cell));
});

async function handleCellClick(cell) {
  if (boardLocked) return;
  if (cell.classList.contains('taken')) return;
  if (isCpuTurn()) return;

  const row = parseInt(cell.dataset.row);
  const col = parseInt(cell.dataset.col);
  await makeMove(row, col);
}

// ── Make move (human) ─────────────────────────
async function makeMove(row, col) {
  if (!sessionId || boardLocked) return;
  boardLocked = true;

  try {
    const data = await apiPost('/api/game/move', { sessionId, row, col });
    if (data.error) { boardLocked = false; return; }

    renderBoard(data.board);
    setTurn(data.currentPlayer);

    if (data.status !== 'ongoing') { handleGameOver(data); return; }

    // If next turn is CPU, trigger it
    if (isCpuTurn()) { await triggerCpuMove(); return; }

    boardLocked = false;
  } catch (err) {
    console.error('Move failed:', err);
    boardLocked = false;
  }
}

// ── Trigger CPU move ──────────────────────────
// row: -1, col: -1 → backend detects CPU turn and picks move
async function triggerCpuMove() {
  boardLocked = true;
  await sleep(600);

  try {
    const data = await apiPost('/api/game/move', { sessionId, row: -1, col: -1 });
    renderBoard(data.board);
    setTurn(data.currentPlayer);

    if (data.status !== 'ongoing') { handleGameOver(data); return; }

    // CC: other CPU's turn
    if (isCpuTurn()) { await triggerCpuMove(); return; }

    boardLocked = false;
  } catch (err) {
    console.error('CPU move failed:', err);
    boardLocked = false;
  }
}

// ── Is current turn a CPU turn? ───────────────
function isCpuTurn() {
  const text = turnIndicator.textContent.trim();
  const isX  = text.startsWith('X');
  const meta = MODE_META[currentMode] || {};
  if (isX)  return !meta.hasX;
  else      return !meta.hasO;
}

// ── Render board from server ──────────────────
function renderBoard(boardData) {
  cells.forEach(cell => { cell.className = 'cell'; cell.textContent = ''; });
  if (!boardData) return;

  let flat = [];
  if (Array.isArray(boardData)) {
    flat = Array.isArray(boardData[0]) ? boardData.flat() : boardData;
  } else if (typeof boardData === 'string') {
    flat = boardData.split('');
  }

  flat.forEach((val, i) => {
    const v = (val || '').toString().trim().toUpperCase();
    if (!v || v === '-' || v === '_' || v === '0') return;
    const row  = Math.floor(i / 3);
    const col  = i % 3;
    const cell = cells.find(c =>
      parseInt(c.dataset.row) === row && parseInt(c.dataset.col) === col
    );
    if (!cell) return;
    cell.textContent = v;
    cell.classList.add('taken', v === 'X' ? 'x-cell' : 'o-cell');
  });
}

// ── Set turn indicator ────────────────────────
function setTurn(player) {
  const p = (player || '').toString().trim().toUpperCase();
  turnIndicator.className = 'turn-indicator';

  const nameX = playerNames.X || 'Player X';
  const nameO = playerNames.O || 'Player O';

  if (p === 'X') {
    turnIndicator.textContent = `${nameX}'S TURN`;
    turnIndicator.classList.add('turn-x');
    document.getElementById('scoreX').classList.add('active-x');
    document.getElementById('scoreO').classList.remove('active-o');
  } else if (p === 'O') {
    turnIndicator.textContent = `${nameO}'S TURN`;
    turnIndicator.classList.add('turn-o');
    document.getElementById('scoreO').classList.add('active-o');
    document.getElementById('scoreX').classList.remove('active-x');
  }
}

// ── Game over ─────────────────────────────────
function handleGameOver(data) {
  boardLocked = true;
  const status = (data.status || '').toLowerCase();
  const winnerRaw = (data.winner || '').toString().trim().toUpperCase();

  resultOverlay.classList.remove('hidden');

  if (status === 'draw' || !winnerRaw) {
    scores.draws++;
    drawCount.textContent = scores.draws + ' draws';
    resultMark.textContent = '—';
    resultMark.className   = 'result-mark draw';
    document.getElementById('resultTitle').textContent = 'DRAW';
    resultMsg.textContent  = 'No winner this round';
  } else {
    const isX = winnerRaw === 'X' ||
                winnerRaw === (playerNames.X || '').toUpperCase();

    if (isX) { scores.X++; scoreXNum.textContent = scores.X; }
    else      { scores.O++; scoreONum.textContent = scores.O; }

    const winName = isX ? playerNames.X : playerNames.O;
    resultMark.textContent = isX ? 'X' : 'O';
    resultMark.className   = `result-mark ${isX ? 'x-win' : 'o-win'}`;
    document.getElementById('resultTitle').textContent = 'GAME OVER';
    resultMsg.textContent  = `${winName} claimed victory`;
  }
}

// ── Play Again ────────────────────────────────
document.getElementById('playAgainBtn').addEventListener('click', async () => {
  if (sessionId) {
    await apiPost('/api/game/reset', { sessionId }).catch(() => {});
    sessionId = null;
  }
  const meta   = MODE_META[currentMode];
  const p1Name = meta.hasX ? playerNames.X : 'Computer';
  const p2Name = meta.hasO ? playerNames.O : 'Computer';
  await startGame(currentMode, p1Name, p2Name);
});

// ── Back to Menu ──────────────────────────────
async function goToMenu() {
  if (sessionId) {
    await apiPost('/api/game/reset', { sessionId }).catch(() => {});
    sessionId = null;
  }
  gameScreen.classList.add('hidden');
  menuScreen.classList.remove('hidden');
  resultOverlay.classList.add('hidden');
  namePanel.classList.add('hidden');
  modeGrid.querySelectorAll('.mode-btn').forEach(b => b.classList.remove('selected'));
  boardLocked  = false;
  pendingMode  = null;
}

document.getElementById('backBtn').addEventListener('click', goToMenu);
document.getElementById('menuBtn').addEventListener('click', goToMenu);

// ── Utility ───────────────────────────────────
function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }

// ── Init ──────────────────────────────────────
checkServer();