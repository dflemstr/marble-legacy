package org.marble.level;

import static org.codehaus.jparsec.Parsers.INDEX;
import static org.codehaus.jparsec.Parsers.between;
import static org.codehaus.jparsec.Parsers.or;
import static org.codehaus.jparsec.Scanners.IDENTIFIER;
import static org.codehaus.jparsec.Scanners.JAVA_BLOCK_COMMENT;
import static org.codehaus.jparsec.Scanners.JAVA_LINE_COMMENT;
import static org.codehaus.jparsec.Scanners.WHITESPACES;
import static org.codehaus.jparsec.Scanners.isChar;
import static org.codehaus.jparsec.Scanners.string;

import java.util.List;

import com.jme3.math.Vector3f;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.misc.Mapper;

import org.marble.level.LevelStatement.Alias;
import org.marble.level.LevelStatement.Connection;
import org.marble.level.LevelStatement.Declaration;
import org.marble.level.LevelStatement.Position;

/**
 * Parser system for serialized levels.
 */
public class LevelParser {
    // Things that are unimportant to our language
    final Parser<Void> delimiter = or(JAVA_LINE_COMMENT, JAVA_BLOCK_COMMENT,
            WHITESPACES).skipMany();

    // A comma.
    final Parser<Void> comma = isChar(',').followedBy(delimiter);

    // A dot.
    final Parser<Void> dot = isChar('.').followedBy(delimiter);

    // An open paren
    final Parser<Void> openParen = isChar('(').followedBy(delimiter);

    // A close paren
    final Parser<Void> closeParen = isChar(')').followedBy(delimiter);

    // A string surrounded by [], like "[abc]". Newlines inside the brackets are
    // supported. Brackets cannot currently be escaped.
    final Parser<Object> string = Scanners.quoted('[', ']')
            .followedBy(delimiter).map(new Stringifier());

    // A positive decimal number like "3" or "3.2" or "0.3" or "-3.".
    final Parser<Object> number = isChar('-').optional().next(Scanners.DECIMAL)
            .source().followedBy(delimiter).map(new Doublifier());

    // A 3-dimensional vector represented as a tuple like "(3, -2, 4.2)"
    final Parser<Vector3f> vector3 = between(
            openParen,
            new Vectorizer().sequence(number.followedBy(comma),
                    number.followedBy(comma), number), closeParen).label(
            "3D vector");
    // Some concrete value literal in the language.
    final Parser<Object> value = or(string, number, vector3).label("value")
            .followedBy(delimiter);
    // A variable identifier like "myvar"
    final Parser<String> identifier = IDENTIFIER.followedBy(delimiter);
    // A Java class name (rough parser) like "java.lang.String"
    final Parser<String> className = IDENTIFIER.sepBy1(string(".")).source()
            .label("class name").followedBy(delimiter);

    // Some constructor args; a tuple of values like "(1, [abc], -3.3)"
    final Parser<ImmutableList<Object>> constrArgs = between(
            openParen.followedBy(delimiter), value.sepBy(comma), closeParen)
            .followedBy(delimiter).map(new ListImmutabilizer<Object>());
    // Misc. keywords
    final Parser<Void> using = string("using").followedBy(delimiter);
    final Parser<Void> as = string("as").followedBy(delimiter);

    final Parser<Void> let = string("let").followedBy(delimiter);
    final Parser<Void> be = string("be").followedBy(delimiter);
    final Parser<Void> position = string("position").followedBy(delimiter);
    final Parser<Void> at = string("at").followedBy(delimiter);

    final Parser<Void> from = string("from").followedBy(delimiter);
    final Parser<Void> connect = string("connect").followedBy(delimiter);
    final Parser<Void> to = string("to").followedBy(delimiter);
    // Parses "using foo as package.Bar, baz as package.Foo"
    final Parser<ImmutableList<Alias>> entityAliases = using.next(Mapper
            .curry(Alias.class).sequence(INDEX, identifier, as.next(className))
            .sepBy(comma).map(new ListImmutabilizer<Alias>()));
    // Parses "let foo be Bar(2, 3), baz be Foo([ba], 34.02)"
    final Parser<ImmutableList<Declaration>> entityDeclarations = let
            .next(Mapper
                    .curry(Declaration.class)
                    .sequence(INDEX, identifier,
                            be.next(identifier.or(className)), constrArgs)
                    .sepBy(comma).map(new ListImmutabilizer<Declaration>()));
    // Parses "connect foo.bar to baz.shizz, bar.baz to bla.gee"
    final Parser<ImmutableList<Connection>> entityLinks = connect.next(Mapper
            .curry(Connection.class)
            .sequence(INDEX, identifier, dot.next(identifier),
                    to.next(identifier), dot.next(identifier)).sepBy(comma)
            .map(new ListImmutabilizer<Connection>()));
    // Parses "position foo at (2, 3, 4), bar at (5, 6, 7) from foo"
    final Parser<ImmutableList<Position>> entityPositions = position
            .next(Mapper
                    .curry(Position.class)
                    .sequence(
                            INDEX,
                            identifier,
                            at.next(vector3),
                            from.next(identifier).optional()
                                    .map(new Optionalizer<String>()))
                    .sepBy(comma).map(new ListImmutabilizer<Position>()));
    // Parses a sequence of statements
    final Parser<ImmutableList<LevelStatement>> parser = delimiter.next(
            or(entityAliases, entityDeclarations, entityLinks, entityPositions)
                    .many().map(new ListFlattener<LevelStatement>()))
            .followedBy(Parsers.EOF);

    public Parser<ImmutableList<LevelStatement>> getParser() {
        return parser;
    }

    /**
     * Flattens a list of immutable lists.
     * 
     * @param <A>
     *            The type of elements in the lists.
     */
    public class ListFlattener<A> implements
            Map<List<ImmutableList<? extends A>>, ImmutableList<A>> {

        @Override
        public ImmutableList<A>
                map(final List<ImmutableList<? extends A>> from) {
            final ImmutableList.Builder<A> builder = ImmutableList.builder();
            for (final ImmutableList<? extends A> list : from) {
                builder.addAll(list);
            }
            return builder.build();
        }

    }

    /**
     * Converts something that might be null into the equivalent
     * {@link Optional} instance.
     * 
     * @param <A>
     *            The type of the value that is optional.
     */
    public class Optionalizer<A> implements Map<A, Optional<A>> {

        @Override
        public Optional<A> map(final A from) {
            return Optional.fromNullable(from);
        }

    }

    /**
     * Converts a String to a Float object.
     */
    private static final class Doublifier implements Map<String, Object> {
        @Override
        public Float map(final String from) {
            return Float.valueOf(from);
        }
    }

    /**
     * Converts any list to an immutable list.
     * 
     * @param <A>
     *            The type of element in the list.
     */
    private static final class ListImmutabilizer<A> implements
            Map<List<A>, ImmutableList<A>> {
        @Override
        public ImmutableList<A> map(final List<A> from) {
            return ImmutableList.copyOf(from);
        }
    }

    /**
     * Restructures a parsed raw string into the string that it quotes.
     */
    private static final class Stringifier implements Map<String, Object> {
        @Override
        public String map(final String from) {
            return from.substring(1, from.length() - 1).replace("\r\n", "\n")
                    .replace("\r", "\n");
        }
    }

    /**
     * Constructs a 3D vector from a sequence of Float parsers.
     */
    private static final class Vectorizer extends Mapper<Vector3f> {
        @SuppressWarnings("unused")
        Vector3f map(final Float x, final Float y, final Float z) {
            return new Vector3f(x, y, z);
        }
    }
}
