-- Enable pgcrypto for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ==========================
-- Table: users
-- ==========================
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       first_name VARCHAR(255) NOT NULL,
                       middle_name VARCHAR(255),
                       last_name VARCHAR(255) NOT NULL,
                       user_name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255),
                       phone_number VARCHAR(15),
                       citizenship_front TEXT,
                       citizenship_back TEXT,
                       dob DATE NOT NULL,
                       is_verified BOOLEAN NOT NULL DEFAULT FALSE,
                       user_gender VARCHAR(50),
                       marital_status VARCHAR(50),
                       role_status VARCHAR(50),
                       created_at TIMESTAMP NOT NULL DEFAULT now(),
                       updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_user_name ON users(user_name);
CREATE INDEX idx_phone_number ON users(phone_number);
CREATE INDEX idx_email ON users(email);

-- ==========================
-- Table: role
-- ==========================
CREATE TABLE role (
                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      role_name VARCHAR(50) NOT NULL UNIQUE,
                      created_at TIMESTAMP NOT NULL DEFAULT now(),
                      updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- ==========================
-- Table: user_roles (join table)
-- ==========================
CREATE TABLE user_roles (
                            user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
                            PRIMARY KEY(user_id, role_id)
);

-- ==========================
-- Table: property (base)
-- ==========================
CREATE TABLE property (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          landlord_id UUID NOT NULL REFERENCES users(id),
                          price NUMERIC(10,2) NOT NULL,
                          description TEXT NOT NULL,
                          district VARCHAR(255) NOT NULL,
                          city VARCHAR(100) NOT NULL,
                          ward VARCHAR(100) NOT NULL,
                          tole VARCHAR(100),
                          rules VARCHAR(2000) NOT NULL,
                          is_available BOOLEAN NOT NULL DEFAULT TRUE,
                          is_verified BOOLEAN NOT NULL DEFAULT FALSE,
                          is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                          property_type VARCHAR(50) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT now(),
                          updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- ==========================
-- Table: house
-- ==========================
CREATE TABLE house (
                       id UUID PRIMARY KEY REFERENCES property(id) ON DELETE CASCADE,
                       floors INT,
                       bed_rooms INT,
                       bath_rooms INT,
                       kitchen INT,
                       living_room INT
);

-- ==========================
-- Table: flat
-- ==========================
CREATE TABLE flat (
                      id UUID PRIMARY KEY REFERENCES property(id) ON DELETE CASCADE,
                      bed_rooms INT,
                      bath_rooms INT,
                      kitchen INT,
                      living_room INT
);

-- ==========================
-- Table: room
-- ==========================
CREATE TABLE room (
                      id UUID PRIMARY KEY REFERENCES property(id) ON DELETE CASCADE
    -- add room-specific fields later if needed
);

-- ==========================
-- Table: amenities
-- ==========================
CREATE TABLE amenities (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           property_id UUID NOT NULL REFERENCES property(id),
                           has_parking BOOLEAN NOT NULL DEFAULT FALSE,
                           has_wifi BOOLEAN NOT NULL DEFAULT FALSE,
                           has_security_staff BOOLEAN NOT NULL DEFAULT FALSE,
                           has_underground_water_tank BOOLEAN NOT NULL DEFAULT FALSE,
                           has_tv BOOLEAN NOT NULL DEFAULT FALSE,
                           has_cctv BOOLEAN NOT NULL DEFAULT FALSE,
                           has_ac BOOLEAN NOT NULL DEFAULT FALSE,
                           has_fridge BOOLEAN NOT NULL DEFAULT FALSE,
                           has_balcony BOOLEAN NOT NULL DEFAULT FALSE,
                           has_water BOOLEAN NOT NULL DEFAULT FALSE,
                           has_solar_water_heater BOOLEAN NOT NULL DEFAULT FALSE,
                           has_fan BOOLEAN NOT NULL DEFAULT FALSE,
                           furnishing_status VARCHAR(50),
                           created_at TIMESTAMP NOT NULL DEFAULT now(),
                           updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- ==========================
-- Table: area
-- ==========================
CREATE TABLE area (
                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      property_id UUID NOT NULL REFERENCES property(id),
                      length FLOAT,
                      breadth FLOAT,
                      total_area FLOAT,
                      created_at TIMESTAMP NOT NULL DEFAULT now(),
                      updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- ==========================
-- Table: assets
-- ==========================
CREATE TABLE assets (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        property_id UUID NOT NULL REFERENCES property(id),
                        asset_type VARCHAR(50) NOT NULL,
                        asset_url VARCHAR(255) NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT now(),
                        updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_assets_property ON assets(property_id);

-- ==========================
-- Table: blogs
-- ==========================
CREATE TABLE blogs (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       title TEXT NOT NULL,
                       content TEXT,
                       author VARCHAR(255) NOT NULL,
                       page_title VARCHAR(255),
                       page_description TEXT,
                       blog_description TEXT,
                       meta_title VARCHAR(255),
                       meta_description TEXT,
                       blog_category VARCHAR(50) NOT NULL,
                       schema JSONB,
                       meta_keywords VARCHAR(1000),
                       slug VARCHAR(255),
                       published BOOLEAN NOT NULL DEFAULT FALSE,
                       blog_image_url TEXT NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT now(),
                       updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_slug ON blogs(slug);
CREATE INDEX idx_author ON blogs(author);
CREATE INDEX idx_title ON blogs(title);
CREATE INDEX idx_category ON blogs(blog_category);

-- ==========================
-- Table: conversations
-- ==========================
CREATE TABLE conversations (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               conversation_type VARCHAR(50) NOT NULL,
                               group_name VARCHAR(255),
                               created_at TIMESTAMP NOT NULL DEFAULT now(),
                               updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- ==========================
-- Table: participant
-- ==========================
CREATE TABLE participants (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              user_id UUID NOT NULL REFERENCES users(id),
                              conversation_id UUID NOT NULL REFERENCES conversations(id),
                              created_at TIMESTAMP NOT NULL DEFAULT now(),
                              updated_at TIMESTAMP NOT NULL DEFAULT now(),
                              CONSTRAINT uk_participant_user_conversation UNIQUE(user_id, conversation_id)
);

-- ==========================
-- Table: message
-- ==========================
CREATE TABLE messages (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          conversation_id UUID NOT NULL REFERENCES conversations(id),
                          sender_id UUID NOT NULL REFERENCES users(id),
                          content TEXT NOT NULL,
                          status VARCHAR(20) NOT NULL DEFAULT 'SENT',
                          created_at TIMESTAMP NOT NULL DEFAULT now(),
                          updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_message_conversation ON messages(conversation_id);
CREATE INDEX idx_message_sender ON messages(sender_id);
CREATE INDEX idx_message_status ON messages(status);

-- ==========================
-- Table: message_attachment
-- ==========================
CREATE TABLE message_attachment (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    message_id UUID NOT NULL REFERENCES messages(id),
                                    file_url TEXT NOT NULL,
                                    file_type VARCHAR(50) NOT NULL,
                                    created_at TIMESTAMP NOT NULL DEFAULT now(),
                                    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_attachment_message ON message_attachment(message_id);

-- ==========================
-- Table: message_reaction
-- ==========================
CREATE TABLE message_reaction (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  message_id UUID NOT NULL REFERENCES messages(id),
                                  user_id UUID NOT NULL REFERENCES users(id),
                                  reaction_type VARCHAR(20) NOT NULL,
                                  created_at TIMESTAMP NOT NULL DEFAULT now(),
                                  updated_at TIMESTAMP NOT NULL DEFAULT now(),
                                  CONSTRAINT uk_reaction_user_message UNIQUE(user_id, message_id)
);

CREATE INDEX idx_reaction_message ON message_reaction(message_id);
CREATE INDEX idx_reaction_user ON message_reaction(user_id);

-- ==========================
-- Table: owner_registration_status
-- ==========================
CREATE TABLE owner_registration_status (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           user_id UUID NOT NULL REFERENCES users(id),
                                           status VARCHAR(50) NOT NULL,
                                           message TEXT,
                                           created_at TIMESTAMP NOT NULL DEFAULT now(),
                                           updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- ==========================
-- Table: payments
-- ==========================
CREATE TABLE payments (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          user_id UUID NOT NULL REFERENCES users(id),
                          property_id UUID NOT NULL REFERENCES property(id),
                          amount NUMERIC(10,2) NOT NULL,
                          payment_date TIMESTAMP NOT NULL DEFAULT now(),
                          expiry_date TIMESTAMP NOT NULL DEFAULT now() + INTERVAL '14 days',
                          payment_method VARCHAR(50) NOT NULL,
                          transaction_id VARCHAR(100) NOT NULL UNIQUE,
                          payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                          is_active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMP NOT NULL DEFAULT now(),
                          updated_at TIMESTAMP NOT NULL DEFAULT now()
);