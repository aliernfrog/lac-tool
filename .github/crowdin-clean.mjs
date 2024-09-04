import { readdirSync, readFileSync, rmSync } from "fs";

const resPath = "./app/src/main/res";

readdirSync(resPath).filter(f => f.startsWith("values-")).forEach(resFile => {
  const folderPath = `${resPath}/${resFile}`;
  const stringsPath = `${folderPath}/strings.xml`;
  const content = readFileSync(stringsPath).toString();
  const lines = content.split("\n");
  console.log(`${stringsPath} has ${lines.length} lines`);
  if (lines.length >= 100) return;
  console.log(`Deleting: ${folderPath}`);
  rmSync(folderPath, { recursive: true, force: true });
});
