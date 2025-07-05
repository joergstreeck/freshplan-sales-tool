#!/bin/bash

echo "⚛️  Starting FreshPlan Frontend with Vite"
echo "======================================="
echo ""

# Check if frontend directory exists
if [ ! -d "frontend" ]; then
    echo "❌ ERROR: frontend directory not found!"
    echo "   Please run from project root directory"
    exit 1
fi

# Check Node.js version
echo "📋 Node.js Version Check:"
node -v
echo ""

# Kill any existing process on port 5173
if lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null; then
    echo "⚠️  Port 5173 is already in use. Stopping existing process..."
    lsof -ti:5173 | xargs kill -9 2>/dev/null
    sleep 2
fi

# Start frontend
cd frontend

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
fi

echo "🚀 Starting Vite Dev Server on port 5173..."
echo "   URL: http://localhost:5173"
echo "   Network: http://192.168.1.42:5173"
echo ""
echo "   Press 'h' for help, 'r' to restart, 'q' to quit"
echo ""

# Start Vite dev server
npm run dev