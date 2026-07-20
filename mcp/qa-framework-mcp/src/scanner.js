import { readdirSync, readFileSync, statSync } from "node:fs";
import { join, extname } from "node:path";

const PACKAGE_RE = /^\s*package\s+([\w.]+)\s*;/m;
const CLASS_RE = /\b(?:public\s+)?(?:abstract\s+)?(?:final\s+)?class\s+(\w+)/;
const TEST_ANNOTATION_RE = /@Test\b/g;
const GROUPS_ARRAY_RE = /groups\s*=\s*\{([^}]*)\}/;
const GROUPS_SINGLE_RE = /groups\s*=\s*"([^"]*)"/;
const METHOD_SIGNATURE_RE =
  /^\s*(?:@\w+(?:\([^)]*\))?\s*)*(?:public|protected|private)?\s*(?:static\s+)?(?:final\s+)?[\w<>[\],.?\s]+?\s+(\w+)\s*\(/;

function listJavaFiles(rootDir) {
  const files = [];

  const walk = (dir) => {
    for (const entry of readdirSync(dir)) {
      const fullPath = join(dir, entry);
      const stats = statSync(fullPath);
      if (stats.isDirectory()) {
        walk(fullPath);
      } else if (stats.isFile() && extname(entry) === ".java") {
        files.push(fullPath);
      }
    }
  };

  walk(rootDir);
  return files;
}

function extractAnnotationBody(source, startIndex) {
  let i = startIndex;
  while (i < source.length && source[i] !== "(" && source[i] !== "\n") {
    if (source[i] === ";" || source.startsWith("class", i)) {
      return { body: "", endIndex: startIndex };
    }
    i += 1;
  }

  if (source[i] !== "(") {
    return { body: "", endIndex: startIndex };
  }

  let depth = 1;
  let j = i + 1;
  while (j < source.length && depth > 0) {
    if (source[j] === "(") depth += 1;
    if (source[j] === ")") depth -= 1;
    j += 1;
  }

  return { body: source.slice(i + 1, j - 1), endIndex: j };
}

function parseGroups(annotationBody) {
  const arrayMatch = annotationBody.match(GROUPS_ARRAY_RE);
  if (arrayMatch) {
    return arrayMatch[1]
      .split(",")
      .map((token) => token.trim().replace(/^"(.*)"$/, "$1"))
      .filter((token) => token.length > 0);
  }

  const singleMatch = annotationBody.match(GROUPS_SINGLE_RE);
  if (singleMatch) {
    return [singleMatch[1]];
  }

  return [];
}

function findMethodNameAfter(source, index) {
  const rest = source.slice(index);
  const lines = rest.split("\n");

  let buffered = "";
  for (const line of lines) {
    const trimmed = line.trim();
    if (trimmed.length === 0) continue;
    if (trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) continue;
    if (trimmed.startsWith("@")) continue;

    buffered += `${line}\n`;
    const match = buffered.match(METHOD_SIGNATURE_RE);
    if (match) {
      return match[1];
    }
    if (trimmed.includes("(")) {
      break;
    }
  }

  return null;
}

function parseJavaFile(filePath, source) {
  const packageMatch = source.match(PACKAGE_RE);
  const classMatch = source.match(CLASS_RE);

  if (!classMatch) {
    return null;
  }

  const packageName = packageMatch ? packageMatch[1] : null;
  const className = classMatch[1];
  const qualifiedName = packageName ? `${packageName}.${className}` : className;

  const methods = [];
  let match;
  TEST_ANNOTATION_RE.lastIndex = 0;
  while ((match = TEST_ANNOTATION_RE.exec(source)) !== null) {
    const annotationEnd = match.index + match[0].length;
    const { body, endIndex } = extractAnnotationBody(source, annotationEnd);
    const groups = parseGroups(body);
    const methodName = findMethodNameAfter(source, endIndex);

    if (methodName) {
      methods.push({ methodName, groups });
    }
  }

  if (methods.length === 0) {
    return null;
  }

  return {
    className: qualifiedName,
    sourceFile: filePath,
    methods,
  };
}

/**
 * Scans a directory tree of Java sources for TestNG @Test methods.
 * Read-only: only reads files, never writes, executes, or modifies anything.
 */
export function scanTestInventory(rootDir) {
  const javaFiles = listJavaFiles(rootDir);
  const classes = [];

  for (const filePath of javaFiles) {
    const source = readFileSync(filePath, "utf8");
    const parsed = parseJavaFile(filePath, source);
    if (parsed) {
      classes.push(parsed);
    }
  }

  classes.sort((a, b) => a.className.localeCompare(b.className));
  return classes;
}
