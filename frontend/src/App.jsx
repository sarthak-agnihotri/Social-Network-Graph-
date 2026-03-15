import React, { useState, useEffect, useRef } from 'react';
import ForceGraph2D from 'react-force-graph-2d';
import './App.css';

// ✅ BASE_URL environment variable se lega, nahi to localhost use karega
const BASE_URL = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8000';

function App() {
  const [graphData, setGraphData] = useState({ nodes: [], links: [] });
  const [highlightNodes, setHighlightNodes] = useState(new Set());
  const [highlightLinks, setHighlightLinks] = useState(new Set());
  const [images, setImages] = useState({}); // Cache for avatar images

  // Forms
  const [userName, setUserName] = useState('');
  const [userEmail, setUserEmail] = useState('');
  const [sourceId, setSourceId] = useState('');
  const [targetId, setTargetId] = useState('');

  // Algorithms
  const [startId, setStartId] = useState('');
  const [endId, setEndId] = useState('');
  const [algoResult, setAlgoResult] = useState(null);

  // Layout Sizing
  const [dimensions, setDimensions] = useState({ width: 800, height: 600 });
  const containerRef = useRef();
  const fgRef = useRef();

  useEffect(() => {
    fetchGraph();

    // Resize Observer
    const resizeObserver = new ResizeObserver(entries => {
      if (entries.length === 0) return;
      const { width, height } = entries[0].contentRect;
      setDimensions({ width, height });
    });

    if (containerRef.current) {
      resizeObserver.observe(containerRef.current);
    }

    return () => resizeObserver.disconnect();
  }, []);

  const fetchGraph = async () => {
    try {
      // ✅ FIXED: Using BASE_URL
      const res = await fetch(`${BASE_URL}/api/graph`);
      const data = await res.json();
      setGraphData(data);
      preloadImages(data.nodes);
    } catch (err) {
      console.error("Error fetching graph:", err);
    }
  };

  const preloadImages = (nodes) => {
    nodes.forEach(node => {
      if (!images[node.id]) {
        const img = new Image();
        // Generate colorful avatar based on name
        img.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(node.name)}&background=random&color=fff&rounded=true&bold=true`;
        img.onload = () => {
          setImages(prev => ({ ...prev, [node.id]: img }));
        };
      }
    });
  };

  const addUser = async () => {
    if (!userName || !userEmail) return;
    const id = Date.now().toString();
    try {
      // ✅ FIXED: Using BASE_URL
      await fetch(`${BASE_URL}/api/user`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id, name: userName, email: userEmail })
      });
      setUserName('');
      setUserEmail('');
      fetchGraph();
    } catch (err) {
      console.error("Error adding user:", err);
    }
  };

  const addFriendship = async () => {
    if (!sourceId || !targetId) return;
    try {
      // ✅ FIXED: Using BASE_URL
      await fetch(`${BASE_URL}/api/friendship`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sourceId, targetId })
      });
      setSourceId('');
      setTargetId('');
      fetchGraph();
    } catch (err) {
      console.error("Error adding friendship:", err);
    }
  };

  const resetGraph = async () => {
    if (!confirm("Are you sure you want to clear the entire graph?")) return;
    try {
      // ✅ FIXED: Using BASE_URL
      await fetch(`${BASE_URL}/api/reset`, {
        method: 'POST'
      });
      setGraphData({ nodes: [], links: [] });
      setHighlightNodes(new Set());
      setHighlightLinks(new Set());
      setAlgoResult(null);
      setImages({});
    } catch (err) {
      console.error("Error resetting graph:", err);
    }
  };

  const getFriendsList = (nodeId) => {
    return graphData.links
      .filter(l => (l.source === nodeId || l.target === nodeId || l.source.id === nodeId || l.target.id === nodeId))
      .map(l => {
        const friendId = (l.source === nodeId || l.source.id === nodeId) ? (l.target.id || l.target) : (l.source.id || l.source);
        return getNameById(friendId);
      });
  };

  const getNameById = (id) => {
    const node = graphData.nodes.find(n => n.id === id);
    return node ? node.name : id;
  };

  const runBFS = async () => {
    if (!startId) return;
    try {
      // ✅ FIXED: Using BASE_URL
      const res = await fetch(`${BASE_URL}/api/bfs`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ startId })
      });
      const pathIds = await res.json();
      const pathNames = pathIds.map(id => getNameById(id));
      setAlgoResult(`BFS Order: ${pathNames.join(' → ')}`);
      animatePath(pathIds);
    } catch (err) {
      console.error("Error running BFS:", err);
    }
  };

  const runDFS = async () => {
    if (!startId) return;
    try {
      // ✅ FIXED: Using BASE_URL
      const res = await fetch(`${BASE_URL}/api/dfs`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ startId })
      });
      const pathIds = await res.json();
      const pathNames = pathIds.map(id => getNameById(id));
      setAlgoResult(`DFS Order: ${pathNames.join(' → ')}`);
      animatePath(pathIds);
    } catch (err) {
      console.error("Error running DFS:", err);
    }
  };

  const runShortestPath = async () => {
    if (!startId || !endId) return;
    try {
      // ✅ FIXED: Using BASE_URL
      const res = await fetch(`${BASE_URL}/api/shortest-path`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ startId, endId })
      });
      const pathIds = await res.json();
      const pathNames = pathIds.map(id => getNameById(id));
      setAlgoResult(`Shortest Path: ${pathNames.join(' → ')}`);
      animatePath(pathIds);
    } catch (err) {
      console.error("Error running Shortest Path:", err);
    }
  };

  const animatePath = (pathIds) => {
    const nodesToHighlight = new Set();
    const linksToHighlight = new Set();

    // Find nodes by ID
    const pathNodes = [];
    pathIds.forEach(id => {
      const node = graphData.nodes.find(n => n.id === id);
      if (node) {
        nodesToHighlight.add(node.id);
        pathNodes.push(node);
      }
    });

    setHighlightNodes(nodesToHighlight);

    // Highlight links
    for (let i = 0; i < pathNodes.length - 1; i++) {
      const source = pathNodes[i];
      const target = pathNodes[i + 1];
      // find link
      const link = graphData.links.find(l =>
        (l.source.id === source.id && l.target.id === target.id) ||
        (l.source.id === target.id && l.target.id === source.id)
      ) || graphData.links.find(l =>
        (l.source === source.id && l.target === target.id) ||
        (l.source === target.id && l.target === source.id)
      );

      if (link) linksToHighlight.add(link);
    }
    setHighlightLinks(linksToHighlight);

    // Auto-center
    if (pathNodes.length > 0 && fgRef.current) {
      fgRef.current.centerAt(pathNodes[0].x, pathNodes[0].y, 1000);
      fgRef.current.zoom(3, 2000);
    }
  };

  return (
    <div className="app-container">

      {/* Sidebar Controls */}
      <div className="sidebar">
        <div>
          <h2>Social Graph</h2>
          <div className="section-title">Manage Users</div>

          <div className="control-group">
            <input className="input-field" placeholder="User Name" value={userName} onChange={e => setUserName(e.target.value)} />
            <input className="input-field" placeholder="Email Address" value={userEmail} onChange={e => setUserEmail(e.target.value)} />
            <button className="btn" onClick={addUser}>+ Add User</button>
          </div>
        </div>

        <div>
          <div className="section-title">Connections</div>
          <div className="control-group">
            <select className="select-field" value={sourceId} onChange={e => setSourceId(e.target.value)}>
              <option value="">Select Source User</option>
              {graphData.nodes.map(n => <option key={n.id} value={n.id}>{n.name}</option>)}
            </select>
            <select className="select-field" value={targetId} onChange={e => setTargetId(e.target.value)}>
              <option value="">Select Friend User</option>
              {graphData.nodes.map(n => <option key={n.id} value={n.id}>{n.name}</option>)}
            </select>
            <button className="btn btn-secondary" onClick={addFriendship}>Connect Users</button>
          </div>
        </div>

        <div>
          <div className="section-title">Algorithms</div>
          <div className="control-group">
            <select className="select-field" value={startId} onChange={e => setStartId(e.target.value)}>
              <option value="">Start User</option>
              {graphData.nodes.map(n => <option key={n.id} value={n.id}>{n.name}</option>)}
            </select>
            <select className="select-field" value={endId} onChange={e => setEndId(e.target.value)}>
              <option value="">End User (Target)</option>
              {graphData.nodes.map(n => <option key={n.id} value={n.id}>{n.name}</option>)}
            </select>

            <div className="algo-grid">
              <button className="btn" onClick={runBFS}>BFS</button>
              <button className="btn" onClick={runDFS}>DFS</button>
              <button className="btn" style={{ gridColumn: "span 2" }} onClick={runShortestPath}>Shortest Path</button>
            </div>

            {algoResult && (
              <div className="result-panel">
                {algoResult}
              </div>
            )}
          </div>
        </div>

        <div>
          <div className="section-title">Network Structure</div>
          <div className="adj-list-container">
            <table className="adj-table">
              <thead>
                <tr>
                  <th>User</th>
                  <th>Friends</th>
                </tr>
              </thead>
              <tbody>
                {graphData.nodes.map(node => (
                  <tr key={node.id}>
                    <td>{node.name}</td>
                    <td>{getFriendsList(node.id).join(', ') || 'None'}</td>
                  </tr>
                ))}
                {graphData.nodes.length === 0 && (
                  <tr>
                    <td colSpan="2" style={{ textAlign: 'center', color: '#64748b' }}>No data</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        <div style={{ marginTop: 'auto' }}>
          <div className="control-group">
            <button className="btn btn-secondary" onClick={() => {
              fgRef.current.zoomToFit(400);
            }}>Reset Camera</button>

            <button className="btn" style={{ background: '#ef4444' }} onClick={resetGraph}>
              ⚠ Clear Graph
            </button>
          </div>
        </div>
      </div>

      {/* Main Visualization Area */}
      <div className="main-content" ref={containerRef}>
        <ForceGraph2D
          ref={fgRef}
          width={dimensions.width}
          height={dimensions.height}
          graphData={graphData}
          backgroundColor="rgba(0,0,0,0)"
          nodeLabel="name"
          nodeColor={node => highlightNodes.has(node.id) ? '#ef4444' : '#3b82f6'}
          nodeRelSize={6}
          linkColor={link => highlightLinks.has(link) ? '#ef4444' : 'rgba(255,255,255,0.2)'}
          linkWidth={link => highlightLinks.has(link) ? 3 : 1}
          linkDirectionalParticles={4}
          linkDirectionalParticleSpeed={d => highlightLinks.has(d) ? 0.01 : 0}
          linkDirectionalParticleWidth={4}

          nodeCanvasObject={(node, ctx, globalScale) => {
            const size = 12; // Radius

            // Draw Avatar Image
            const img = images[node.id];
            if (img) {
              ctx.save();
              ctx.beginPath();
              ctx.arc(node.x, node.y, size, 0, 2 * Math.PI, false);
              ctx.clip();
              try {
                ctx.drawImage(img, node.x - size, node.y - size, size * 2, size * 2);
              } catch (e) { }
              ctx.restore();

              // Border
              ctx.beginPath();
              ctx.arc(node.x, node.y, size, 0, 2 * Math.PI, false);
              ctx.lineWidth = highlightNodes.has(node.id) ? 3 : 1;
              ctx.strokeStyle = highlightNodes.has(node.id) ? '#ef4444' : '#fff';
              ctx.stroke();

            } else {
              // Fallback Circle
              ctx.beginPath();
              ctx.arc(node.x, node.y, 5, 0, 2 * Math.PI, false);
              ctx.fillStyle = highlightNodes.has(node.id) ? '#ef4444' : '#3b82f6';
              ctx.fill();
            }

            // Glow effect
            if (highlightNodes.has(node.id)) {
              ctx.shadowBlur = 10;
              ctx.shadowColor = "#ef4444";
            } else {
              ctx.shadowBlur = 0;
            }

            node.__bckgDimensions = [size * 2, size * 2];

            // Label
            const label = node.name;
            const fontSize = 14 / globalScale;
            ctx.font = `600 ${fontSize}px Inter`;
            const textWidth = ctx.measureText(label).width;
            const bckgDimensions = [textWidth, fontSize].map(n => n + fontSize * 0.5);

            // Label Background
            ctx.fillStyle = 'rgba(15, 23, 42, 0.8)';
            ctx.fillRect(node.x - bckgDimensions[0] / 2, node.y + size + 4, ...bckgDimensions);

            // Label Text
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillStyle = '#fff';
            ctx.fillText(label, node.x, node.y + size + 4 + bckgDimensions[1] / 2);

            ctx.shadowBlur = 0; // Reset shadow
          }}
          nodePointerAreaPaint={(node, color, ctx) => {
            const size = 12;
            ctx.fillStyle = color;
            const bckgDimensions = node.__bckgDimensions;

            // Paint node circle
            ctx.beginPath();
            ctx.arc(node.x, node.y, size, 0, 2 * Math.PI, false);
            ctx.fill();
          }}
        />
        <div className="graph-overlay">
          Interactive Graph • Scroll to Zoom • Drag to Move • {graphData.nodes.length} Nodes • {graphData.links.length} Links
        </div>
      </div>
    </div>
  );
}

export default App;
