package de.freshplan.integration.sync;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeckCli {
  private static final Logger log = LoggerFactory.getLogger(DeckCli.class);
  private final String deckBin;
  private final String kongAddr;

  public DeckCli(String deckBin, String kongAddr){
    this.deckBin = deckBin; this.kongAddr = kongAddr;
  }

  public String diff(String file) throws IOException, InterruptedException {
    Process p = new ProcessBuilder(deckBin, "--kong-addr", kongAddr, "diff", "-s", file)
        .redirectErrorStream(true).start();
    String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    p.waitFor();
    return out;
  }

  public String sync(String file) throws IOException, InterruptedException {
    Process p = new ProcessBuilder(deckBin, "--kong-addr", kongAddr, "sync", "-s", file)
        .redirectErrorStream(true).start();
    String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    p.waitFor();
    return out;
  }
}
