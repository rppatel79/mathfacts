import { useEffect, useMemo, useRef, useState } from "react";

type SpellingItem = { word: string; sentence: string };
type TtsRequest = { testId: string; items: SpellingItem[] };
type AudioInfo = { word: string; sentenceUrl: string; promptUrl: string };
type TtsResponse = { testId: string; results: AudioInfo[] };

type Row = {
  idx: number;
  word: string;
  audioUrl: string;      // using combined prompt (promptUrl)
  userAnswer: string;
  correct: boolean | null;
};

function normalize(s: string) {
  return s.trim().toLowerCase();
}
function getQueryParam(name: string) {
  const url = new URL(window.location.href);
  return url.searchParams.get(name) ?? "";
}
function formatDuration(ms: number) {
  const totalSec = Math.max(0, Math.floor(ms / 1000));
  const m = Math.floor(totalSec / 60);
  const s = totalSec % 60;
  return `${m}:${s.toString().padStart(2, "0")}`;
}

export default function SpellingTest() {
  const [testId, setTestId] = useState(getQueryParam("testId"));
  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<Row[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [activeIdx, setActiveIdx] = useState<number | null>(null);
  const [startedAt, setStartedAt] = useState<number | null>(null);
  const [elapsedMs, setElapsedMs] = useState<number | null>(null);
  const [submitted, setSubmitted] = useState(false);

  const audioRef = useRef<HTMLAudioElement | null>(null);
  const inputsRef = useRef<Record<number, HTMLInputElement | null>>({});

  const total = rows.length;
  const correctCount = useMemo(
    () => rows.reduce((acc, r) => acc + (r.correct ? 1 : 0), 0),
    [rows]
  );

  useEffect(() => {
    if (testId) void loadTest();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadTest() {
    if (!testId.trim()) return;
    setLoading(true);
    setError(null);
    setRows([]);
    setActiveIdx(null);
    setSubmitted(false);
    setElapsedMs(null);

    try {
      const r1 = await fetch(`/api/spelling/tests/${encodeURIComponent(testId)}`);
      if (!r1.ok) throw new Error(`Test not found: ${testId}`);
      const testJson = (await r1.json()) as TtsRequest;

      const r2 = await fetch(`/api/spelling/tts`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(testJson),
      });
      if (!r2.ok) throw new Error(`TTS request failed (${r2.status})`);
      const tts = (await r2.json()) as TtsResponse;

      const nextRows: Row[] = tts.results.map((res, idx) => ({
        idx,
        word: res.word,
        audioUrl: res.promptUrl || res.sentenceUrl,
        userAnswer: "",
        correct: null,
      }));
      setRows(nextRows);
      setActiveIdx(0);
      setStartedAt(Date.now());

      // play first & focus first input
      setTimeout(() => {
        play(0);
        inputsRef.current[0]?.focus();
      }, 60);
    } catch (e: any) {
      setError(e?.message || String(e));
    } finally {
      setLoading(false);
    }
  }

  function play(idx: number) {
    const a = audioRef.current;
    if (!a || !rows[idx]) return;
    a.src = rows[idx].audioUrl;
    a.currentTime = 0;
    a.play().catch(() => {});
    setActiveIdx(idx);
  }

  function updateAnswer(idx: number, value: string) {
    setRows(prev => prev.map(r => (r.idx === idx ? { ...r, userAnswer: value } : r)));
  }

  function submitAll() {
    setRows(prev =>
      prev.map(r => ({ ...r, correct: normalize(r.userAnswer) === normalize(r.word) }))
    );
    setSubmitted(true);
    if (startedAt) setElapsedMs(Date.now() - startedAt);
  }

  return (
    <div className="wrap">
      <h1>Spelling Test</h1>

      <div className="toolbar">
        <input
          value={testId}
          onChange={e => setTestId(e.target.value)}
          placeholder="testId (e.g., week-2025-09-01)"
          onKeyDown={e => { if (e.key === "Enter") void loadTest(); }}
          disabled={loading}
        />
        <button disabled={loading || !testId.trim()} onClick={loadTest}>
          {loading ? "Loading…" : "Start"}
        </button>

        <div className="spacer" />
        {total > 0 && (
          <div className="score">
            Score: <b>{correctCount}</b> / {total}
            {elapsedMs != null && <> • Time: <b>{formatDuration(elapsedMs)}</b></>}
          </div>
        )}
      </div>

      {error && <div className="error">{error}</div>}

      {rows.length > 0 && (
        <>
          <div className="audioBar">
            <audio ref={audioRef} controls />
            {activeIdx !== null && (
              <>
                <button onClick={() => play(activeIdx)}>▶ Play</button>
                <button onClick={() => activeIdx! > 0 && play(activeIdx! - 1)}>⏮ Prev</button>
                <button onClick={() => activeIdx! < rows.length - 1 && play(activeIdx! + 1)}>⏭ Next</button>
              </>
            )}
          </div>

          <ol className="list">
            {rows.map((r, i) => {
              const state = r.correct === null ? "" : r.correct ? "ok" : "bad";
              return (
                <li key={r.idx} className={`card ${state}`}>
                  <div className="row">
                    <button onClick={() => play(i)}>▶</button>
                    <span className="muted">Item {i + 1}</span>
                    <span className="flex1" />
                    {submitted && r.correct === true && <span className="ok">✓ Correct</span>}
                    {submitted && r.correct === false && (
                      <span className="bad">✗ Incorrect — answer is “{r.word}”</span>
                    )}
                  </div>

                  <div className="row">
                    <input
                      ref={(el) => { inputsRef.current[i] = el; }}
                      value={r.userAnswer}
                      onChange={e => updateAnswer(i, e.target.value)}
                      onKeyDown={e => {
                        if (e.key === "Enter" && i < rows.length - 1) {
                          inputsRef.current[i + 1]?.focus();
                        }
                      }}
                      placeholder="Type the spelling"
                      disabled={submitted}
                    />
                  </div>
                </li>
              );
            })}
          </ol>

          <div className="footer">
            <button className="primary" onClick={submitAll} disabled={submitted}>
              {submitted ? "Submitted" : "Submit All"}
            </button>
          </div>
        </>
      )}
    </div>
  );
}
