# Social Network Graph Visualizer

A full-stack application for visualizing social network graphs using Java for the backend (REST API) and Vite/React for the frontend (Force-Directed Graph).

## Project Structure

- `backend/`: Java source code for the graph data structures, algorithms (BFS, DFS, Shortest Path), and a simple HTTP server.
- `frontend/`: React application using `react-force-graph-2d` for interactive visualization.

## Prerequisites

- **Java Development Kit (JDK)**: Version 8 or higher.
- **Node.js**: Version 18 or higher.
- **npm**: (Included with Node.js).

## Run Instructions

### 1. Backend Server

The easiest way to run the backend is using the provided script from the `backend/` directory:

```powershell
.\run.ps1
```

*(This script automatically handles folder creation, compilation, and starting the server.)*

Alternatively, you can run the commands manually:
```powershell
# A. Create bin folder
if (!(Test-Path bin)) { New-Item -ItemType Directory -Path bin }

# B. Create file list
$files = Get-ChildItem -Path src -Filter *.java -Recurse | Select-Object -ExpandProperty FullName
[System.IO.File]::WriteAllLines("$pwd\sources.txt", $files)

# C. Compile and Run
javac -d bin "@sources.txt"
java -cp bin com.socialgraph.server.SimpleHttpServer
```

The server will start on `http://localhost:8000`.

### 2. Frontend Application

Run these commands from the `frontend/` directory:

```bash
# 1. Install dependencies (only required once)
npm install

# 2. Start the development server
npm run dev
```

The frontend will be available at the URL shown in your terminal (usually `http://localhost:5173`).

## API Endpoints

- `GET /api/graph`: Returns the current graph (nodes and links).
- `POST /api/user`: Adds a new user.
- `POST /api/friendship`: Creates a friendship between two users.
- `POST /api/bfs`: Runs Breadth-First Search starting from a user ID.
- `POST /api/dfs`: Runs Depth-First Search starting from a user ID.
- `POST /api/shortest-path`: Finds the shortest path between two user IDs.
- `POST /api/reset`: Clears the graph data.
