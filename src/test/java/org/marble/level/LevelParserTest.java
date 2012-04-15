package org.marble.level;

import static org.junit.Assert.assertEquals;

import com.jme3.math.Vector3f;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import org.marble.level.LevelStatement.Alias;
import org.marble.level.LevelStatement.Connection;
import org.marble.level.LevelStatement.Declaration;
import org.marble.level.LevelStatement.Position;

public class LevelParserTest {

    private LevelParser parser;
    // Indicates that the constructor that takes this as a parameter doesn't use
    // that parameter for equality, so it could be anything for testing
    // purposes.
    private final int IGNORED = 0;

    @Test
    public void className() {
        assertEquals("java.util.List",
                parser.className.parse("java.util.List  "));
    }

    @Test
    public void delimiter() {
        parser.delimiter.parse(" /* test */ // test\n\r\f\t  ");
    }

    @Test
    public void entityAliases() {
        assertEquals(
                ImmutableList.of(new LevelStatement.Alias(IGNORED, "String",
                        "java.lang.String")),
                parser.entityAliases
                        .parse("using String as java.lang.String  // Not actually an entity"));
    }

    @Test
    public void entityDeclarations() {
        assertEquals(ImmutableList.of(new LevelStatement.Declaration(IGNORED,
                "ball1", "String", ImmutableList.of((Object) "abc",
                        (Object) 123.0f, (Object) "abc"))),
                parser.entityDeclarations
                        .parse("let ball1 be String ( [abc], 123, [abc] )   "));
    }

    @Test
    public void entityLinks() {
        assertEquals(ImmutableList.of(new LevelStatement.Connection(IGNORED,
                "a", "ac", "b", "bc")),
                parser.entityLinks.parse("connect a.ac to b.bc    "));
    }

    @Test
    public void entityPositions() {
        final Optional<String> absent = Optional.absent();
        assertEquals(ImmutableList.of(new LevelStatement.Position(IGNORED, "a",
                new Vector3f(1, 2, 3), absent)),
                parser.entityPositions.parse("position a at ( 1, 2, 3)"));
        assertEquals(ImmutableList.of(new LevelStatement.Position(IGNORED, "a",
                new Vector3f(1, 2, 3), Optional.of("b"))),
                parser.entityPositions.parse("position a at ( 1, 2, 3) from b"));
    }

    @Test
    public void identifier() {
        assertEquals("test", parser.identifier.parse("test  "));
    }

    @Test
    public void number() {
        assertEquals(2.0f, parser.number.parse("2  "));
        assertEquals(2.5f, parser.number.parse("2.5  "));
        assertEquals(0.5f, parser.number.parse("0.5  "));
        assertEquals(-0.5f, parser.number.parse("-0.5  "));
    }

    @Test
    public void parser() {
        final String level =
                "using String as java.lang.String, String2 as java.lang.String\n"
                        + "/* This is just a dysfunctional test level for our unit test */\n"
                        + "let s1 be String(1, 2.0, [3]), s2 be String2()\n"
                        + "position s1 at (0, 0, 0)\n // The starting platform\n"
                        + "connect s1.a to s2.b, s2.a to s1.b";
        final Optional<String> absent = Optional.absent();
        assertEquals(ImmutableList.of(new Alias(IGNORED, "String",
                "java.lang.String"), new Alias(IGNORED, "String2",
                "java.lang.String"), new Declaration(IGNORED, "s1", "String",
                ImmutableList.of((Object) 1.0f, (Object) 2.0f, (Object) "3")),
                new Declaration(IGNORED, "s2", "String2", ImmutableList.of()),
                new Position(IGNORED, "s1", new Vector3f(0, 0, 0), absent),
                new Connection(IGNORED, "s1", "a", "s2", "b"), new Connection(
                        IGNORED, "s2", "a", "s1", "b")),
                parser.parser.parse(level));
    }

    @Before
    public void setUp() {
        parser = new LevelParser();
    }

    @Test
    public void string() {
        assertEquals("string\na\nb\nc",
                parser.string.parse("[string\r\na\nb\rc]  "));
    }

    @Test
    public void vector3() {
        assertEquals(new Vector3f(2.0f, 3.2f, 4.6f),
                parser.vector3.parse("(2, 3.2, 4.6)"));
    }
}
