#!/bin/bash

# Update HTML versions of documentation
echo "📝 Updating HTML documentation..."

# Check if pandoc is installed
if ! command -v pandoc &> /dev/null; then
    echo "❌ pandoc is not installed. Please install it first:"
    echo "   brew install pandoc"
    exit 1
fi

# Update WAY_OF_WORKING.html
echo "Converting WAY_OF_WORKING.md to HTML..."
pandoc WAY_OF_WORKING.md \
    -f markdown \
    -t html \
    --standalone \
    --metadata title="FreshPlan Way of Working" \
    --toc \
    --toc-depth=3 \
    --css=https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.2.0/github-markdown-min.css \
    -o WAY_OF_WORKING.html

# Add GitHub markdown CSS wrapper
cat > temp.html << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>FreshPlan Way of Working</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.2.0/github-markdown-min.css">
    <style>
        .markdown-body {
            box-sizing: border-box;
            min-width: 200px;
            max-width: 980px;
            margin: 0 auto;
            padding: 45px;
        }
        @media (max-width: 767px) {
            .markdown-body {
                padding: 15px;
            }
        }
        pre {
            background-color: #f6f8fa;
            border-radius: 6px;
            padding: 16px;
            overflow: auto;
        }
        code {
            background-color: rgba(175,184,193,0.2);
            padding: 0.2em 0.4em;
            border-radius: 3px;
            font-size: 85%;
        }
        table {
            border-collapse: collapse;
            width: 100%;
            margin: 1em 0;
        }
        table th, table td {
            border: 1px solid #dfe2e5;
            padding: 6px 13px;
        }
        table tr:nth-child(2n) {
            background-color: #f6f8fa;
        }
    </style>
</head>
<body>
    <article class="markdown-body">
EOF

# Extract body content from pandoc output and append
sed -n '/<body>/,/<\/body>/p' WAY_OF_WORKING.html | sed '1d;$d' >> temp.html

# Close HTML
cat >> temp.html << 'EOF'
    </article>
</body>
</html>
EOF

# Replace original file
mv temp.html WAY_OF_WORKING.html

echo "✅ Documentation updated successfully!"
echo ""
echo "Files updated:"
echo "  - WAY_OF_WORKING.html"
echo ""
echo "You can view the HTML file by opening it in a browser:"
echo "  open WAY_OF_WORKING.html"