package net.quined.textual_filters;

import static junit.framework.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class CorpusTest {
  private Corpus smallCorpus;
  @Before
  public void setUp() {
    smallCorpus = new Corpus();
  }

  @Test
  public void loadSmallString() {
  assertEquals(true, true);
  }
}
